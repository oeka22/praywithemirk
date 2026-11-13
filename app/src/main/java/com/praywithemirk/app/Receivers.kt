package com.praywithemirk.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.Instant
import java.time.ZoneId

/** Empfängt die geplanten Alarme. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getIntExtra(AlarmScheduler.EXTRA_PRAYER, -1)
        val time = intent.getLongExtra(AlarmScheduler.EXTRA_TIME, 0L)
        val now = System.currentTimeMillis()

        when (intent.action) {
            AlarmScheduler.ACTION_ADHAN -> {
                // Nur abspielen, wenn der Alarm nicht stark verspätet ist (z. B. nach langem Tiefschlaf).
                if (prayer >= 0 && now - time < 10 * 60_000L) {
                    AdhanService.start(context, prayer, fromAlarm = true)
                }
            }
            AlarmScheduler.ACTION_REMINDER -> {
                if (prayer >= 0) Notifications.showReminder(context, prayer)
            }
        }
        // Nächsten Alarm planen – strikt nach dem gerade ausgelösten Zeitpunkt.
        val after = Instant.ofEpochMilli(maxOf(now, time) + 1000).atZone(ZoneId.systemDefault())
        AlarmScheduler.reschedule(context, after)
    }
}

/** Plant nach Neustart, Update, Zeit-/Zeitzonenänderung neu. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.reschedule(context)
    }
}
