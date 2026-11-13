package com.praywithemirk.app

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** Berechnung der Gebetsrichtung zur Kaaba. */
object Qibla {
    const val KAABA_LAT = 21.422487
    const val KAABA_LNG = 39.826206

    /** Richtung zur Kaaba in Grad ab geografisch Nord (im Uhrzeigersinn). */
    fun bearing(lat: Double, lng: Double): Double {
        val p1 = Math.toRadians(lat)
        val p2 = Math.toRadians(KAABA_LAT)
        val dl = Math.toRadians(KAABA_LNG - lng)
        val b = Math.toDegrees(atan2(sin(dl), cos(p1) * tan(p2) - sin(p1) * cos(dl)))
        return (b + 360.0) % 360.0
    }

    /** Entfernung zur Kaaba in km (Großkreis). */
    fun distanceKm(lat: Double, lng: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }
}

/** Kompass, der immer Richtung Mekka zeigt. */
class QiblaActivity : Activity(), SensorEventListener {

    private lateinit var prefs: Prefs
    private lateinit var sm: SensorManager
    private lateinit var compass: CompassView
    private lateinit var info: TextView
    private lateinit var hint: TextView
    private lateinit var live: TextView
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var lastEvent = 0L

    private var qibla = 0.0
    private var declination = 0f
    private var heading = 0f
    private var smoothed = Float.NaN
    private var aligned = false

    private val grav = FloatArray(3)
    private val geo = FloatArray(3)
    private var hasGrav = false
    private var hasGeo = false
    private var useRotationVector = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        C.applyTo(this, prefs.theme)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sm = getSystemService(SensorManager::class.java)
        val l = prefs.lang

        qibla = Qibla.bearing(prefs.lat, prefs.lng)
        declination = GeomagneticField(
            prefs.lat.toFloat(), prefs.lng.toFloat(), 0f, System.currentTimeMillis()
        ).declination

        val root = FrameLayout(this)
        root.addView(PatternBackground(this), FrameLayout.LayoutParams(-1, -1))
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(28))
            fitsSystemWindows = true
        }
        root.addView(col, FrameLayout.LayoutParams(-1, -1))

        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(text("‹", 32f, C.gold, Fonts.light).apply {
            setPadding(dp(4), 0, dp(16), dp(4)); setOnClickListener { finish() }
        })
        head.addView(text(S.qibla[l], 22f, C.cream, Fonts.light))
        col.addView(head)

        col.addView(text(prefs.place.ifBlank { "%.3f°, %.3f°".format(prefs.lat, prefs.lng) }.uppercase(), 12f, C.gold, Fonts.medium, Gravity.CENTER)
            .apply { letterSpacing = 0.1f }, lp(top = dp(16)))

        compass = CompassView(this)
        col.addView(compass, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(12); bottomMargin = dp(12) })

        info = text("", 16f, C.cream, Fonts.light, Gravity.CENTER)
        col.addView(info)
        live = text("", 22f, C.gold, Fonts.light, Gravity.CENTER).apply { fontFeatureSettings = "tnum" }
        col.addView(live, lp(top = dp(6)))
        hint = text("", 13f, C.muted, Fonts.light, Gravity.CENTER)
        col.addView(hint, lp(top = dp(8)))

        val km = Qibla.distanceKm(prefs.lat, prefs.lng).roundToInt()
        info.text = S.qiblaInfo[l].format(qibla.roundToInt(), km)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        hint.text = S.compassHint[prefs.lang]
        smoothed = Float.NaN
        lastEvent = 0L
        val rv = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rv != null && sm.registerListener(this, rv, SensorManager.SENSOR_DELAY_GAME)) {
            useRotationVector = true
            // Kommt vom Rotationssensor nach 2 s nichts an, auf Beschleunigung + Magnetfeld wechseln
            handler.postDelayed({ if (lastEvent == 0L) { sm.unregisterListener(this); useAccMag() } }, 2000)
        } else useAccMag()
        // Zeigt nach 4 s ohne Daten einen klaren Hinweis
        handler.postDelayed({ if (lastEvent == 0L) { hint.text = S.noCompassData[prefs.lang]; hint.setTextColor(C.gold) } }, 4000)
    }

    private fun useAccMag() {
        useRotationVector = false
        hasGrav = false; hasGeo = false
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (acc == null || mag == null) {
            hint.text = S.noCompass[prefs.lang]
            return
        }
        sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME)
        sm.registerListener(this, mag, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        sm.unregisterListener(this)
    }

    override fun onSensorChanged(e: SensorEvent) {
        val r = FloatArray(9)
        val o = FloatArray(3)
        when (e.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(r, e.values)
                SensorManager.getOrientation(r, o)
                update(Math.toDegrees(o[0].toDouble()).toFloat())
            }
            Sensor.TYPE_ACCELEROMETER -> {
                lowPass(e.values, grav); hasGrav = true
                fromAccMag(r, o)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                lowPass(e.values, geo); hasGeo = true
                fromAccMag(r, o)
            }
        }
    }

    private fun fromAccMag(r: FloatArray, o: FloatArray) {
        if (!hasGrav || !hasGeo || useRotationVector) return
        if (SensorManager.getRotationMatrix(r, null, grav, geo)) {
            SensorManager.getOrientation(r, o)
            update(Math.toDegrees(o[0].toDouble()).toFloat())
        }
    }

    private fun lowPass(input: FloatArray, out: FloatArray) {
        for (i in 0..2) out[i] = out[i] + 0.15f * (input[i] - out[i])
    }

    /** [magneticAzimuth] in Grad; wird in geografische Richtung umgerechnet und geglättet. */
    private fun update(magneticAzimuth: Float) {
        if (magneticAzimuth.isNaN()) return
        lastEvent = System.currentTimeMillis()
        val trueHeading = ((magneticAzimuth + declination) % 360f + 360f) % 360f
        smoothed = if (smoothed.isNaN()) trueHeading else {
            var d = trueHeading - smoothed
            if (d > 180f) d -= 360f
            if (d < -180f) d += 360f
            ((smoothed + d * 0.2f) % 360f + 360f) % 360f
        }
        heading = smoothed
        var diff = (qibla.toFloat() - heading)
        diff = ((diff + 540f) % 360f) - 180f
        val nowAligned = abs(diff) < 3f
        if (nowAligned && !aligned) compass.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        aligned = nowAligned
        compass.set(heading, qibla.toFloat(), aligned)
        val turn = diff.roundToInt()
        live.text = when {
            aligned -> "✓  " + S.facingQibla[prefs.lang]
            turn > 0 -> S.turnRight[prefs.lang].format(turn)
            else -> S.turnLeft[prefs.lang].format(-turn)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD || sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val l = prefs.lang
            hint.text = if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) S.calibrate[l] else S.compassHint[l]
            hint.setTextColor(if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) C.gold else C.muted)
        }
    }
}

/** Zeichnet die Kompassrose und den Zeiger zur Kaaba. */
class CompassView(context: Context) : View(context) {
    private var heading = 0f
    private var qibla = 0f
    private var aligned = false

    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = context.dp(2f).toFloat(); color = alpha(C.cream, 60) }
    private val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = context.dp(1.2f).toFloat(); color = alpha(C.cream, 90) }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = C.cream; textAlign = Paint.Align.CENTER; typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textSize = context.dp(16f).toFloat()
    }
    private val north = Paint(label).apply { color = C.gold }
    private val needle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = C.gold }
    private val kaaba = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.parseColor("#141414") }
    private val kaabaEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = context.dp(1.2f).toFloat(); color = C.cream }
    private val kaabaBand = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = C.gold }
    private val marker = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = C.muted }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = alpha(C.gold, 40) }
    private val path = Path()

    fun set(heading: Float, qibla: Float, aligned: Boolean) {
        this.heading = heading; this.qibla = qibla; this.aligned = aligned
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f - context.dp(24)

        if (aligned) c.drawCircle(cx, cy, r + context.dp(14), glow)

        // fester Marker oben = Blickrichtung des Handys
        path.reset()
        val m = context.dp(10f).toFloat()
        path.moveTo(cx, cy - r - context.dp(4)); path.lineTo(cx - m, cy - r - m * 2.2f); path.lineTo(cx + m, cy - r - m * 2.2f); path.close()
        c.drawPath(path, if (aligned) needle else marker)

        // Rose dreht sich gegen die Blickrichtung
        c.save()
        c.rotate(-heading, cx, cy)
        c.drawCircle(cx, cy, r, ring)
        for (i in 0 until 72) {
            val a = Math.toRadians(i * 5.0)
            val len = if (i % 18 == 0) r * 0.10f else if (i % 6 == 0) r * 0.06f else r * 0.03f
            c.drawLine(
                cx + (r * sin(a)).toFloat(), cy - (r * cos(a)).toFloat(),
                cx + ((r - len) * sin(a)).toFloat(), cy - ((r - len) * cos(a)).toFloat(), tick
            )
        }
        val labels = arrayOf("N", "O", "S", "W")
        for (i in 0..3) {
            val a = Math.toRadians(i * 90.0)
            val tr = r * 0.78f
            c.drawText(labels[i], cx + (tr * sin(a)).toFloat(), cy - (tr * cos(a)).toFloat() + label.textSize / 3, if (i == 0) north else label)
        }

        // Zeiger zur Kaaba
        c.rotate(qibla, cx, cy)
        path.reset()
        val w = r * 0.07f
        path.moveTo(cx, cy - r * 0.62f)
        path.lineTo(cx - w, cy)
        path.lineTo(cx + w, cy)
        path.close()
        c.drawPath(path, needle)
        c.drawCircle(cx, cy, w * 1.2f, needle)
        // Kaaba-Symbol an der Spitze
        val ks = r * 0.11f
        val ky = cy - r * 0.62f - ks * 1.4f
        c.drawRect(cx - ks, ky - ks, cx + ks, ky + ks, kaaba)
        c.drawRect(cx - ks, ky - ks * 0.45f, cx + ks, ky - ks * 0.15f, kaabaBand)
        c.drawRect(cx - ks, ky - ks, cx + ks, ky + ks, kaabaEdge)
        c.restore()
    }
}
