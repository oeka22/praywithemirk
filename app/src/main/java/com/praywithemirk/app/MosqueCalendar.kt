package com.praywithemirk.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Übernimmt den Jahreskalender einer Moschee von ihrer öffentlichen Mawaqit-Seite.
 * Die Seite enthält die Gebetszeiten als JSON („confData“ → „calendar“: 12 Monate × Tage × 6 Zeiten).
 * Der Kalender wird lokal gespeichert und alle 14 Tage still aktualisiert.
 */
object MosqueCalendar {

    class Preset(val name: String, val url: String, val note: T? = null)

    val presets = listOf(
        Preset("Haymana Moschee (Jenfeld)", "https://mawaqit.net/de/m/haymana-moschee-jenfeld-hamburg-22043-germany"),
        Preset("Assahaba Moschee", "https://mawaqit.net/de/assahaba-hamburg"),
        Preset("Al-Nour Moschee", "https://mawaqit.net/de/m/al-nour-hamburg"),
        Preset(
            "Merkez Camii (IGMG)", "https://mawaqit.net/de/m/haymana-moschee-jenfeld-hamburg-22043-germany",
            T(
                "Nicht auf Mawaqit. Der IGMG-Kalender für Hamburg stimmt mit dem der Haymana Moschee überein (geprüft: ±1 Minute), daher wird dieser verwendet.",
                "Mawaqit'te yok. IGMG'nin Hamburg takvimi Haymana Camii'ninkiyle aynı (kontrol edildi: ±1 dakika), bu yüzden o kullanılır.",
                "Not on Mawaqit. The IGMG calendar for Hamburg matches the Haymana mosque (checked: ±1 minute), so that one is used."
            )
        ),
    )

    class Cal(val name: String, val url: String, val fetchedAt: Long, private val months: JSONArray) {
        /** Die 6 Zeiten (Fajr, Shuruq, Dhuhr, Asr, Maghrib, Isha) als "HH:MM" oder null. */
        fun day(date: LocalDate): List<String>? = try {
            val m = months.getJSONObject(date.monthValue - 1)
            var key = date.dayOfMonth.toString()
            if (!m.has(key) && date.monthValue == 2 && date.dayOfMonth == 29) key = "28"
            val a = m.getJSONArray(key)
            if (a.length() < 6) null else (0 until 6).map { a.getString(it) }
        } catch (_: Exception) { null }
    }

    private var cache: Cal? = null
    private var cacheStamp = -1L

    private fun file(ctx: Context) = File(ctx.applicationContext.filesDir, "mosque_calendar.json")

    fun load(ctx: Context): Cal? {
        val f = file(ctx)
        if (!f.exists()) return null
        if (cache != null && cacheStamp == f.lastModified()) return cache
        return try {
            val o = JSONObject(f.readText())
            Cal(o.getString("name"), o.getString("url"), o.optLong("fetchedAt"), o.getJSONArray("calendar")).also {
                cache = it; cacheStamp = f.lastModified()
            }
        } catch (_: Exception) { null }
    }

    /** Zeiten eines Tages aus dem Moschee-Kalender (oder null, wenn nicht vorhanden). */
    fun timesFor(ctx: Context, date: LocalDate, zone: ZoneId): List<ZonedDateTime>? {
        val cal = load(ctx) ?: return null
        val d = cal.day(date) ?: return null
        return try {
            d.map { s -> date.atTime(LocalTime.parse(s.trim().padStart(5, '0'))).atZone(zone) }
        } catch (_: Exception) { null }
    }

    /**
     * Lädt den Kalender von einer Mawaqit-Seite. Rückgabe im Haupt-Thread:
     * Name der Moschee oder null bei Fehler.
     */
    fun fetch(ctx: Context, url: String, displayName: String?, cb: (String?) -> Unit) {
        val app = ctx.applicationContext
        val main = Handler(Looper.getMainLooper())
        Thread {
            val name = try {
                val html = download(url)
                val conf = extractConf(html)
                val cal = conf.getJSONArray("calendar")
                require(cal.length() == 12)
                val n = displayName ?: conf.optString("name").ifBlank { url }
                val out = JSONObject()
                    .put("name", n).put("url", url).put("fetchedAt", System.currentTimeMillis())
                    .put("calendar", cal)
                file(app).writeText(out.toString())
                n
            } catch (_: Exception) { null }
            main.post { cb(name) }
        }.start()
    }

    /** Alle 14 Tage still aktualisieren (Moscheen passen ihre Kalender gelegentlich an). */
    fun refreshIfStale(ctx: Context) {
        val cal = load(ctx) ?: return
        if (System.currentTimeMillis() - cal.fetchedAt > 14L * 24 * 3600 * 1000) {
            fetch(ctx, cal.url, cal.name) { if (it != null) AlarmScheduler.reschedule(ctx.applicationContext) }
        }
    }

    fun clear(ctx: Context) {
        file(ctx).delete(); cache = null
    }

    fun isMawaqitUrl(url: String): Boolean = try {
        val h = URL(url).host.lowercase()
        URL(url).protocol == "https" && (h == "mawaqit.net" || h.endsWith(".mawaqit.net"))
    } catch (_: Exception) { false }

    private fun download(start: String): String {
        var url = URL(start)
        var redirects = 0
        while (true) {
            require(url.protocol == "https")
            val c = url.openConnection() as HttpURLConnection
            c.instanceFollowRedirects = false
            c.connectTimeout = 15_000; c.readTimeout = 20_000
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) PrayWithEmirK/1.0")
            val code = c.responseCode
            if (code in 300..399 && redirects < 5) {
                url = URL(url, c.getHeaderField("Location")); c.disconnect(); redirects++; continue
            }
            require(code == 200)
            val text = c.inputStream.bufferedReader().use { it.readText() }
            c.disconnect()
            return text
        }
    }

    /** Sucht „confData = { … }“ im Seitenquelltext und liest das JSON-Objekt (Klammern zählen). */
    private fun extractConf(html: String): JSONObject {
        val m = Regex("confData\\s*=\\s*\\{").find(html) ?: error("no confData")
        val start = m.range.last
        var depth = 0
        var inStr = false
        var esc = false
        var i = start
        while (i < html.length) {
            val ch = html[i]
            if (inStr) {
                if (esc) esc = false
                else if (ch == '\\') esc = true
                else if (ch == '"') inStr = false
            } else {
                when (ch) {
                    '"' -> inStr = true
                    '{' -> depth++
                    '}' -> { depth--; if (depth == 0) return JSONObject(html.substring(start, i + 1)) }
                }
            }
            i++
        }
        error("unterminated")
    }
}
