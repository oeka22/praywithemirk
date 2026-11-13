package com.praywithemirk.app

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Eigene Gebetsruf-Aufnahmen über das Mikrofon (max. 3 Minuten). */
object Recordings {
    const val MAX_MS = 180_000

    class Rec(val id: String, val name: String, val file: File) {
        val uri: String get() = Uri.fromFile(file).toString()
    }

    private fun dir(ctx: Context) = File(ctx.filesDir, "recordings").apply { mkdirs() }
    private fun sp(ctx: Context) = ctx.getSharedPreferences("praywithemirk", Context.MODE_PRIVATE)

    fun list(ctx: Context): List<Rec> {
        val arr = try { JSONArray(sp(ctx).getString("recordings", "[]")) } catch (_: Exception) { JSONArray() }
        val out = ArrayList<Rec>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val f = File(dir(ctx), o.optString("file"))
            if (f.exists()) out.add(Rec(o.optString("id"), o.optString("name"), f))
        }
        return out
    }

    private fun save(ctx: Context, list: List<Rec>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("id", it.id).put("name", it.name).put("file", it.file.name)) }
        sp(ctx).edit().putString("recordings", arr.toString()).apply()
    }

    /** Neue, noch unbenannte Zieldatei für eine Aufnahme. */
    fun newFile(ctx: Context): File = File(dir(ctx), "rec_${System.currentTimeMillis()}.m4a")

    fun add(ctx: Context, file: File, name: String): Rec {
        val r = Rec(file.nameWithoutExtension, name.ifBlank { file.nameWithoutExtension }, file)
        save(ctx, list(ctx) + r)
        return r
    }

    /** Löscht die Aufnahme und entfernt sie überall, wo sie als Gebetsruf eingestellt war. */
    fun delete(ctx: Context, rec: Rec) {
        val prefs = Prefs(ctx)
        val uri = rec.uri
        if (prefs.adhanUri == uri) { prefs.adhanUri = null; prefs.adhanName = null }
        for (i in P.ALL) if (prefs.prayerUri(i) == uri) prefs.setPrayerSound(i, null, null)
        rec.file.delete()
        save(ctx, list(ctx).filter { it.id != rec.id })
    }

    @Suppress("DEPRECATION")
    fun recorder(ctx: Context, out: File): MediaRecorder {
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(ctx) else MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(128_000)
        r.setAudioSamplingRate(44_100)
        r.setMaxDuration(MAX_MS)
        r.setOutputFile(out.absolutePath)
        r.prepare()
        return r
    }
}
