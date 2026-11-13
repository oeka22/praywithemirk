package com.praywithemirk.app

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lädt einen Gebetsruf von einem direkten Audio-Link herunter und speichert ihn
 * im App-Speicher, damit er zur Gebetszeit auch ohne Internet abgespielt werden kann.
 */
object AudioDownloader {
    private const val MAX_BYTES = 25L * 1024 * 1024

    fun isYouTube(url: String): Boolean {
        val h = try { Uri.parse(url).host?.lowercase() ?: "" } catch (_: Exception) { "" }
        return h.endsWith("youtube.com") || h.endsWith("youtu.be") || h.endsWith("youtube-nocookie.com")
    }

    /** @param slot Dateiname-Zusatz (z. B. "default" oder Gebetsindex). Rückgabe: file://-URI oder null. */
    fun download(ctx: Context, url: String, slot: String, cb: (String?) -> Unit) {
        val main = Handler(Looper.getMainLooper())
        Thread {
            val result = try { fetch(ctx, url, slot) } catch (_: Exception) { null }
            main.post { cb(result) }
        }.start()
    }

    private fun fetch(ctx: Context, startUrl: String, slot: String): String? {
        var url = URL(startUrl)
        var conn: HttpURLConnection
        var redirects = 0
        while (true) {
            if (url.protocol != "https") return null
            conn = url.openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.setRequestProperty("User-Agent", "PrayWithEmirK/1.0 (Android)")
            val code = conn.responseCode
            if (code in 300..399 && redirects < 5) {
                val loc = conn.getHeaderField("Location") ?: return null
                url = URL(url, loc)
                conn.disconnect()
                redirects++
                continue
            }
            if (code != 200) { conn.disconnect(); return null }
            break
        }
        val type = conn.contentType?.lowercase() ?: ""
        if (type.startsWith("text/") || type.contains("html")) { conn.disconnect(); return null }
        if (conn.contentLengthLong > MAX_BYTES) { conn.disconnect(); return null }

        val dir = File(ctx.filesDir, "adhan").apply { mkdirs() }
        val tmp = File(dir, "dl_$slot.tmp")
        var total = 0L
        conn.inputStream.use { input ->
            tmp.outputStream().use { out ->
                val buf = ByteArray(16 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > MAX_BYTES) return null.also { tmp.delete() }
                    out.write(buf, 0, n)
                }
            }
        }
        conn.disconnect()
        if (total < 10_000) { tmp.delete(); return null } // zu klein für einen Gebetsruf
        val target = File(dir, "adhan_$slot")
        target.delete()
        if (!tmp.renameTo(target)) return null
        return Uri.fromFile(target).toString()
    }

    /** Dateiname aus einem Link für die Anzeige. */
    fun nameFrom(url: String): String {
        val seg = try { Uri.parse(url).lastPathSegment } catch (_: Exception) { null }
        return (seg?.takeIf { it.isNotBlank() } ?: Uri.parse(url).host ?: "Link") + " (Link)"
    }
}
