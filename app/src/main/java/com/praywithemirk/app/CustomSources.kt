package com.praywithemirk.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Eigene Quellen, die per URL geladen werden.
 * Unterstützt: JSON (Liste von Einträgen), einfache Textdateien (Einträge durch Leerzeilen getrennt)
 * und – als Notlösung – normale Webseiten (werden in Absätze zerlegt).
 */
object CustomSources {

    class Source(val id: String, val name: String, val url: String, val enabled: Boolean, val count: Int, val fetchedAt: Long)
    class Item(val arabic: String, val de: String, val tr: String, val en: String, val ref: String)

    private fun sp(ctx: Context) = ctx.getSharedPreferences("praywithemirk", Context.MODE_PRIVATE)
    private fun dir(ctx: Context) = File(ctx.filesDir, "sources").apply { mkdirs() }

    fun list(ctx: Context): List<Source> {
        val arr = try { JSONArray(sp(ctx).getString("custom_sources", "[]")) } catch (_: Exception) { JSONArray() }
        return (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let {
                Source(it.optString("id"), it.optString("name"), it.optString("url"),
                    it.optBoolean("enabled", true), it.optInt("count"), it.optLong("fetchedAt"))
            }
        }
    }

    private fun save(ctx: Context, list: List<Source>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("name", it.name).put("url", it.url)
                .put("enabled", it.enabled).put("count", it.count).put("fetchedAt", it.fetchedAt))
        }
        sp(ctx).edit().putString("custom_sources", arr.toString()).apply()
    }

    fun setEnabled(ctx: Context, id: String, on: Boolean) =
        save(ctx, list(ctx).map { if (it.id == id) Source(it.id, it.name, it.url, on, it.count, it.fetchedAt) else it })

    fun delete(ctx: Context, id: String) {
        File(dir(ctx), "$id.json").delete()
        save(ctx, list(ctx).filter { it.id != id })
    }

    /** Inhalte aller aktiven eigenen Quellen als Tagesimpulse. */
    fun enabledItems(ctx: Context): List<DailyItem> = list(ctx).filter { it.enabled }.flatMap { src ->
        loadItems(ctx, src.id).map { it.toDaily(src.name) }
    }

    private fun Item.toDaily(name: String): DailyItem {
        val fallback = de.ifBlank { en.ifBlank { tr } }
        return DailyItem(
            false, T(name, name, name), arabic, null,
            T(de.ifBlank { fallback }, tr.ifBlank { fallback }, en.ifBlank { fallback }),
            if (ref.isBlank()) name else "$name · $ref"
        )
    }

    private fun loadItems(ctx: Context, id: String): List<Item> = try {
        val arr = JSONArray(File(dir(ctx), "$id.json").readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Item(o.optString("ar"), o.optString("de"), o.optString("tr"), o.optString("en"), o.optString("ref"))
        }
    } catch (_: Exception) { emptyList() }

    private fun storeItems(ctx: Context, id: String, items: List<Item>) {
        val arr = JSONArray()
        items.forEach { arr.put(JSONObject().put("ar", it.arabic).put("de", it.de).put("tr", it.tr).put("en", it.en).put("ref", it.ref)) }
        File(dir(ctx), "$id.json").writeText(arr.toString())
    }

    /** Neue Quelle laden und speichern. Rückgabe im Haupt-Thread: Anzahl Einträge (0 = fehlgeschlagen). */
    fun add(ctx: Context, name: String, url: String, cb: (Int) -> Unit) {
        val id = "c" + System.currentTimeMillis()
        fetchAsync(url) { items ->
            if (items.isNotEmpty()) {
                storeItems(ctx, id, items)
                save(ctx, list(ctx) + Source(id, name.ifBlank { hostOf(url) }, url, true, items.size, System.currentTimeMillis()))
            }
            cb(items.size)
        }
    }

    fun refresh(ctx: Context, id: String, cb: (Int) -> Unit) {
        val src = list(ctx).firstOrNull { it.id == id } ?: return cb(0)
        fetchAsync(src.url) { items ->
            if (items.isNotEmpty()) {
                storeItems(ctx, id, items)
                save(ctx, list(ctx).map {
                    if (it.id == id) Source(it.id, it.name, it.url, it.enabled, items.size, System.currentTimeMillis()) else it
                })
            }
            cb(items.size)
        }
    }

    /** Alle Quellen, die älter als eine Woche sind, still im Hintergrund aktualisieren. */
    fun refreshStale(ctx: Context) {
        val week = 7L * 24 * 3600 * 1000
        list(ctx).filter { System.currentTimeMillis() - it.fetchedAt > week }.forEach { refresh(ctx.applicationContext, it.id) {} }
    }

    private fun hostOf(url: String) = try { URL(url).host } catch (_: Exception) { url }

    private fun fetchAsync(url: String, cb: (List<Item>) -> Unit) {
        val main = Handler(Looper.getMainLooper())
        Thread {
            val items = try { parse(download(url)) } catch (_: Exception) { emptyList() }
            main.post { cb(items) }
        }.start()
    }

    private fun download(start: String): String {
        var url = URL(start)
        var redirects = 0
        while (true) {
            require(url.protocol == "https")
            val c = url.openConnection() as HttpURLConnection
            c.instanceFollowRedirects = false
            c.connectTimeout = 15_000; c.readTimeout = 20_000
            c.setRequestProperty("User-Agent", "PrayWithEmirK/1.0 (Android)")
            val code = c.responseCode
            if (code in 300..399 && redirects < 5) {
                url = URL(url, c.getHeaderField("Location")); c.disconnect(); redirects++; continue
            }
            require(code == 200)
            val bytes = c.inputStream.use { it.readNBytesCompat(2 * 1024 * 1024) }
            c.disconnect()
            return String(bytes, Charsets.UTF_8)
        }
    }

    private fun java.io.InputStream.readNBytesCompat(max: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(16 * 1024)
        while (out.size() < max) {
            val n = read(buf); if (n < 0) break
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    // ---------------------------------------------------------------- Einlesen

    private val arabicRe = Regex("[\\u0600-\\u06FF]")

    fun parse(raw: String): List<Item> {
        val t = raw.trim().removePrefix("﻿")
        return when {
            t.startsWith("[") || t.startsWith("{") -> parseJson(t)
            t.contains("<html", true) || t.contains("<p", true) -> parseHtml(t)
            else -> parseText(t)
        }.filter { it.arabic.isNotBlank() || it.de.isNotBlank() || it.en.isNotBlank() || it.tr.isNotBlank() }.take(500)
    }

    private fun JSONObject.pick(vararg keys: String): String {
        for (k in keys) {
            val v = opt(k)
            if (v is String && v.isNotBlank()) return v.trim()
            if (v is JSONObject) { // z. B. {"text": {"de": …}}
                val inner = v.optString("text", "")
                if (inner.isNotBlank()) return inner.trim()
            }
        }
        return ""
    }

    private fun parseJson(t: String): List<Item> {
        val arr: JSONArray = if (t.startsWith("[")) JSONArray(t) else {
            val o = JSONObject(t)
            o.keys().asSequence().mapNotNull { o.optJSONArray(it) }.firstOrNull() ?: JSONArray().put(o)
        }
        return (0 until arr.length()).mapNotNull { i ->
            when (val e = arr.opt(i)) {
                is String -> textItem(e)
                is JSONObject -> {
                    val generic = e.pick("text", "translation", "body", "content", "hadith", "matn")
                    var ar = e.pick("ar", "arabic", "arab", "text_ar", "arabicText")
                    if (ar.isBlank() && arabicRe.containsMatchIn(generic)) ar = generic
                    val gen = if (generic == ar) "" else generic
                    Item(
                        ar,
                        e.pick("de", "german", "deutsch", "text_de").ifBlank { gen },
                        e.pick("tr", "turkish", "turkce", "text_tr"),
                        e.pick("en", "english", "text_en"),
                        e.pick("source", "ref", "reference", "quelle", "kaynak", "book")
                    )
                }
                else -> null
            }
        }
    }

    private fun textItem(block: String): Item? {
        val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null
        var ref = ""
        val body = lines.toMutableList()
        val last = body.last()
        if (body.size > 1 && (last.startsWith("—") || last.startsWith("-") || last.startsWith("Quelle:", true) || last.startsWith("Kaynak:", true) || last.startsWith("Source:", true))) {
            ref = last.trimStart('—', '-', ' ').substringAfter(':', last.trimStart('—', '-', ' ')).trim()
            body.removeAt(body.size - 1)
        }
        val ar = body.filter { arabicRe.containsMatchIn(it) && it.count { c -> c in '؀'..'ۿ' } > it.length / 3 }
        val rest = body - ar.toSet()
        return Item(ar.joinToString(" "), rest.joinToString(" "), "", "", ref)
    }

    private fun parseText(t: String): List<Item> =
        t.split(Regex("\\n\\s*\\n")).mapNotNull { textItem(it) }

    private fun parseHtml(t: String): List<Item> {
        var s = t.replace(Regex("(?is)<(script|style|nav|header|footer)[^>]*>.*?</\\1>"), " ")
        s = s.replace(Regex("(?i)<br\\s*/?>|</p>|</div>|</li>|</h\\d>|</blockquote>"), "\n\n")
        s = s.replace(Regex("<[^>]+>"), " ")
        s = s.replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'")
            .replace("&lt;", "<").replace("&gt;", ">")
        return s.split(Regex("\\n\\s*\\n"))
            .map { it.replace(Regex("[ \\t\\x0B\\f\\r]+"), " ").trim() }
            .filter { it.length in 40..700 }
            .take(200)
            .mapNotNull { textItem(it) }
    }
}
