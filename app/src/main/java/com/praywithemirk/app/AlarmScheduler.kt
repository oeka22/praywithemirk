package com.praywithemirk.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.ZoneId
import java.time.ZonedDateTime

/** Plant jeweils den nächsten Gebetsruf und (optional) die nächste Vorab-Erinnerung. */
object AlarmScheduler {
    const val ACTION_ADHAN = "com.praywithemirk.app.action.ADHAN"
    const val ACTION_REMINDER = "com.praywithemirk.app.action.REMINDER"
    const val EXTRA_PRAYER = "prayer"
    const val EXTRA_TIME = "time"

    private const val RC_ADHAN = 101
    private const val RC_REMINDER = 102

    fun canScheduleExact(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = ctx.getSystemService(AlarmManager::class.java)
        return am.canScheduleExactAlarms()
    }

    /**
     * Plant neu. [after] = Zeitpunkt, nach dem gesucht wird (Standard: jetzt).
     * Gibt das nächste geplante Adhan-Ereignis zurück (oder null).
     */
    fun reschedule(ctx: Context, after: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())): PrayerEvent? {
        val prefs = Prefs(ctx)
        val am = ctx.getSystemService(AlarmManager::class.java)
        val events = Schedule.events(prefs, after, 3)

        val nextAdhan = events.firstOrNull { it.time.isAfter(after) && prefs.adhanEnabled(it.prayer) }
        val adhanPi = pending(ctx, ACTION_ADHAN, RC_ADHAN, nextAdhan)
        if (nextAdhan != null) set(am, ctx, nextAdhan.time.toInstant().toEpochMilli(), adhanPi)
        else am.cancel(adhanPi)

        val minutes = prefs.reminderMinutes
        val nextReminder = if (minutes > 0) events.firstOrNull {
            prefs.adhanEnabled(it.prayer) && it.time.minusMinutes(minutes.toLong()).isAfter(after)
        } else null
        val remPi = pending(ctx, ACTION_REMINDER, RC_REMINDER, nextReminder)
        if (nextReminder != null) {
            set(am, ctx, nextReminder.time.minusMinutes(minutes.toLong()).toInstant().toEpochMilli(), remPi)
        } else am.cancel(remPi)

        return nextAdhan
    }

    private fun pending(ctx: Context, action: String, rc: Int, ev: PrayerEvent?): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java).setAction(action)
        if (ev != null) {
            i.putExtra(EXTRA_PRAYER, ev.prayer)
            i.putExtra(EXTRA_TIME, ev.time.toInstant().toEpochMilli())
        }
        return PendingIntent.getBroadcast(
            ctx, rc, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun set(am: AlarmManager, ctx: Context, at: Long, pi: PendingIntent) {
        try {
            if (canScheduleExact(ctx)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
}
