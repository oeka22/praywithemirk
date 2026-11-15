package com.praywithemirk.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Merkt sich, ob gerade eine Seite der App sichtbar ist (für das direkte Öffnen des Adhan-Bildschirms). */
class App : Application() {
    companion object {
        @Volatile var visibleActivities = 0
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(a: Activity) { visibleActivities++ }
            override fun onActivityStopped(a: Activity) { visibleActivities = maxOf(0, visibleActivities - 1) }
            override fun onActivityCreated(a: Activity, b: Bundle?) {}
            override fun onActivityResumed(a: Activity) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })
    }
}

/**
 * Vollbild während des Gebetsrufs: animierte Glocke (10 Sekunden), Stopp-Knopf
 * und – falls erlaubt – Stoppen durch dreimaliges Klatschen.
 */
class AdhanActivity : Activity() {

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val ACTION_FINISHED = "com.praywithemirk.app.action.ADHAN_FINISHED"
        const val BELL_MS = 10_000L
    }

    private lateinit var prefs: Prefs
    private var prayer = P.DHUHR
    private var clap: ClapDetector? = null
    private lateinit var clapInfo: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val finishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isFinishing) finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        C.applyTo(this, prefs.theme)
        prayer = intent.getIntExtra(EXTRA_PRAYER, P.DHUHR)

        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val l = prefs.lang
        val root = FrameLayout(this)
        root.addView(PatternBackground(this), FrameLayout.LayoutParams(-1, -1))
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(40))
            fitsSystemWindows = true
        }
        root.addView(col, FrameLayout.LayoutParams(-1, -1))

        val bell = BellView(this)
        col.addView(bell, LinearLayout.LayoutParams(dp(240), dp(240)).apply { gravity = Gravity.CENTER_HORIZONTAL })
        if (Prefs(this).showArabic) col.addView(text(S.prayerArabic[prayer], 40f, C.gold, Fonts.serif, Gravity.CENTER), lp(top = dp(8)))
        col.addView(text(S.timeFor[l].format(S.prayerNames[prayer][l]), 26f, C.cream, Fonts.light, Gravity.CENTER), lp(top = dp(4)))
        col.addView(text(java.time.LocalTime.now().withSecond(0).withNano(0).toString(), 18f, C.muted, Fonts.light, Gravity.CENTER), lp(top = dp(2)))

        col.addView(pill("■  " + S.stop[l], filled = true) { stopAdhan() }.apply {
            textSize = 18f
            setPadding(dp(36), dp(14), dp(36), dp(14))
        }, LinearLayout.LayoutParams(-2, -2).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(32) })

        clapInfo = text("", 14f, C.muted, Fonts.light, Gravity.CENTER)
        col.addView(clapInfo, lp(top = dp(16)))

        col.addView(text(S.tapForAdhkar[l], 13f, C.teal, Fonts.regular, Gravity.CENTER).apply {
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setOnClickListener {
                startActivity(Intent(this@AdhanActivity, DhikrActivity::class.java).putExtra(DhikrActivity.EXTRA_PRAYER, prayer))
            }
        }, lp(top = dp(12)))

        setContentView(root)
        bell.start(BELL_MS)
        // Nach 10 Sekunden schließt die Glockenanzeige; der Gebetsruf läuft weiter bis zum Ende
        // (Stopp über Startseite, Benachrichtigung oder 3× Klatschen).
        handler.postDelayed({ if (!isFinishing) finish() }, BELL_MS)

        val filter = IntentFilter(ACTION_FINISHED)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(finishedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        else @Suppress("UnspecifiedRegisterReceiverFlag") registerReceiver(finishedReceiver, filter)

        if (!AdhanService.playing) handler.postDelayed({ if (!AdhanService.playing) finish() }, 1500)
    }

    override fun onResume() {
        super.onResume()
        val l = prefs.lang
        if (prefs.clapToStop && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            clapInfo.text = S.clapHint[l]
            clap = ClapDetector(
                onClap = { n -> clapInfo.text = S.clapHint[l] + "   " + "●".repeat(n) + "○".repeat(3 - n) },
                onTriple = { stopAdhan() }
            ).also { it.start() }
        } else clapInfo.text = ""
    }

    override fun onPause() {
        super.onPause()
        clap?.stop(); clap = null
    }

    override fun onDestroy() {
        try { unregisterReceiver(finishedReceiver) } catch (_: Exception) {}
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun stopAdhan() {
        AdhanService.stop(this)
        finish()
    }
}

/** Glocke, die 10 Sekunden schwingt, mit pulsierenden Lichtwellen. */
class BellView(context: Context) : View(context) {
    private var startAt = 0L
    private var duration = 0L
    private val body = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wave = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }

    fun start(ms: Long) {
        duration = ms
        startAt = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2; val cy = h / 2
        val s = min(w, h)
        val t = (SystemClock.uptimeMillis() - startAt).coerceAtLeast(0L)
        val running = t < duration
        val sec = t / 1000f
        val fade = if (running) 1f else 0f

        // Hintergrundschein
        glow.shader = RadialGradient(cx, cy, s * 0.5f, alpha(C.gold, if (running) 90 else 50), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, s * 0.5f, glow)

        // Lichtwellen
        if (running) {
            for (k in 0..2) {
                val phase = ((sec * 0.8f + k / 3f) % 1f)
                wave.strokeWidth = s * 0.012f
                wave.color = alpha(C.gold, (180 * (1 - phase) * fade).toInt())
                c.drawCircle(cx, cy, s * (0.22f + phase * 0.28f), wave)
            }
        }

        // Schwingen: gedämpfte Sinusbewegung
        val angle = if (running) (sin(sec * 2 * Math.PI * 1.3).toFloat() * 20f * (1f - 0.5f * sec / (duration / 1000f))) else 0f
        val blink = if (running) (0.75f + 0.25f * sin(sec * 2 * Math.PI * 2).toFloat()) else 1f

        c.save()
        c.rotate(angle, cx, cy - s * 0.22f)
        val bw = s * 0.36f; val top = cy - s * 0.24f; val bottom = cy + s * 0.14f
        path.reset()
        path.moveTo(cx - bw * 0.12f, top)
        path.cubicTo(cx - bw * 0.55f, top + s * 0.02f, cx - bw * 0.5f, bottom - s * 0.12f, cx - bw * 0.62f, bottom)
        path.lineTo(cx + bw * 0.62f, bottom)
        path.cubicTo(cx + bw * 0.5f, bottom - s * 0.12f, cx + bw * 0.55f, top + s * 0.02f, cx + bw * 0.12f, top)
        path.close()
        body.shader = android.graphics.LinearGradient(cx - bw, top, cx + bw, bottom,
            lighter(C.gold, 0.35f), darker(C.gold, 0.25f), Shader.TileMode.CLAMP)
        body.alpha = (255 * blink).toInt()
        body.setShadowLayer(s * 0.05f, 0f, 0f, alpha(C.gold, 200))
        c.drawPath(path, body)
        body.shader = null; body.clearShadowLayer()
        body.color = darker(C.gold, 0.15f)
        c.drawRoundRect(cx - bw * 0.7f, bottom - s * 0.01f, cx + bw * 0.7f, bottom + s * 0.035f, s * 0.02f, s * 0.02f, body)
        c.drawCircle(cx, top - s * 0.02f, s * 0.03f, body)
        // Klöppel schwingt leicht gegenläufig
        c.rotate(-angle * 0.6f, cx, bottom)
        c.drawCircle(cx, bottom + s * 0.07f, s * 0.035f, body)
        c.restore()

        if (running) postInvalidateOnAnimation()
    }
}

/**
 * Erkennt kurze, laute Geräusche (Klatschen) über das Mikrofon.
 * Drei Klatscher innerhalb von 3 Sekunden lösen [onTriple] aus.
 * Der Hintergrundpegel (z. B. der laufende Gebetsruf) wird laufend gemessen,
 * damit nur deutliche Spitzen zählen.
 */
class ClapDetector(private val onClap: (Int) -> Unit, private val onTriple: () -> Unit) {
    @Volatile private var running = false
    private var thread: Thread? = null
    private val main = Handler(Looper.getMainLooper())

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return
        running = true
        thread = Thread {
            val rate = 16000
            val minBuf = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val rec = try {
                AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, 4096))
            } catch (e: Exception) { null }
            if (rec == null || rec.state != AudioRecord.STATE_INITIALIZED) { running = false; return@Thread }
            val buf = ShortArray(512) // ca. 32 ms
            var background = 300.0
            var lastClap = 0L
            val claps = ArrayList<Long>()
            try {
                rec.startRecording()
                while (running) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n <= 0) continue
                    var peak = 0; var sum = 0.0
                    for (i in 0 until n) { val v = abs(buf[i].toInt()); if (v > peak) peak = v; sum += v.toDouble() * v }
                    val rms = sqrt(sum / n)
                    val now = SystemClock.uptimeMillis()
                    val isClap = peak > 9000 && rms > background * 3.5 && now - lastClap > 180
                    if (isClap) {
                        lastClap = now
                        claps.add(now)
                        claps.removeAll { now - it > 3000 }
                        val count = claps.size
                        main.post { onClap(minOf(count, 3)) }
                        if (count >= 3) {
                            running = false
                            main.post { onTriple() }
                        }
                    } else {
                        // langsam anpassen, Spitzen nicht einrechnen
                        background = background * 0.95 + rms * 0.05
                    }
                }
            } catch (_: Exception) {
            } finally {
                try { rec.stop() } catch (_: Exception) {}
                rec.release()
            }
        }.apply { start() }
    }

    fun stop() {
        running = false
        thread = null
    }
}
