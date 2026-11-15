package com.praywithemirk.app

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import android.view.WindowManager
import java.time.LocalTime

/**
 * Nachtmodus für die Startseite (Wanduhr): dimmt nachts die Bildschirmhelligkeit dieses Fensters
 * und legt zusätzlich einen dunklen Schleier darüber – nach Uhrzeit oder per Lichtsensor.
 * Ändert keine Systemeinstellungen; außerhalb der App gilt die normale Helligkeit.
 */
class Dimmer(private val activity: Activity) : SensorEventListener {

    companion object {
        fun hasLightSensor(ctx: Context): Boolean =
            ctx.getSystemService(SensorManager::class.java)?.getDefaultSensor(Sensor.TYPE_LIGHT) != null

        /** Liegt die Stunde im Zeitraum [from, to)? Über Mitternacht möglich (z. B. 22 → 6). */
        fun inWindow(hour: Int, from: Int, to: Int): Boolean = when {
            from == to -> false
            from < to -> hour in from until to
            else -> hour >= from || hour < to
        }

        private val BRIGHTNESS = floatArrayOf(0.01f, 0.03f, 0.10f) // sehr dunkel, dunkel, gedämpft
        private val VEIL = floatArrayOf(0.55f, 0.30f, 0.0f)
        private const val DARK_BELOW_LUX = 4f
        private const val BRIGHT_ABOVE_LUX = 12f
    }

    var overlay: View? = null
        set(v) { field = v; applied = null }

    /** Zuletzt gemessene (geglättete) Helligkeit in Lux, -1 = unbekannt. */
    var lux = -1f
        private set
    private var dark = false
    private var registered = false
    private var applied: Int? = null // null = noch nichts angewendet, -1 = normal, sonst Stufe

    fun start(prefs: Prefs) {
        if (prefs.nightMode != 2 || registered) return
        val sm = activity.getSystemService(SensorManager::class.java) ?: return
        val s = sm.getDefaultSensor(Sensor.TYPE_LIGHT) ?: return
        registered = sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        if (!registered) return
        activity.getSystemService(SensorManager::class.java)?.unregisterListener(this)
        registered = false
    }

    /** Jede Sekunde aufrufen: entscheidet, ob gedimmt wird. [forceBright] z. B. während des Gebetsrufs. */
    fun update(prefs: Prefs, now: LocalTime, forceBright: Boolean) {
        val mode = prefs.nightMode
        if (mode == 2 && !registered) start(prefs)
        if (mode != 2 && registered) stop()
        val night = when {
            forceBright || mode == 0 -> false
            mode == 2 && registered && lux >= 0f -> {
                if (dark && lux > BRIGHT_ABOVE_LUX) dark = false
                else if (!dark && lux < DARK_BELOW_LUX) dark = true
                dark
            }
            mode == 2 && registered -> false // noch kein Messwert
            else -> inWindow(now.hour, prefs.nightFrom, prefs.nightTo)
        }
        apply(if (night) prefs.nightLevel.coerceIn(0, 2) else -1)
    }

    /** Beim Verlassen der Seite: normale Helligkeit. */
    fun reset() = apply(-1)

    private fun apply(level: Int) {
        if (applied == level) return
        applied = level
        val lp = activity.window.attributes
        lp.screenBrightness = if (level < 0) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE else BRIGHTNESS[level]
        activity.window.attributes = lp
        overlay?.animate()?.alpha(if (level < 0) 0f else VEIL[level])?.setDuration(1500)?.start()
    }

    override fun onSensorChanged(e: SensorEvent) {
        val v = e.values.firstOrNull() ?: return
        lux = if (lux < 0f) v else lux * 0.8f + v * 0.2f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
