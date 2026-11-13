package com.praywithemirk.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color

object Notifications {
    const val CH_ADHAN = "adhan"
    const val CH_REMINDER = "reminder"
    const val CH_INFO = "after_adhan"

    const val ID_ADHAN = 1
    const val ID_AFTER = 2
    const val ID_REMINDER = 3

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val l = Prefs(ctx).lang
        val adhan = NotificationChannel(
            CH_ADHAN, S.adhanSection[l], NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null) // Ton kommt vom Dienst
            enableVibration(false)
            lightColor = Color.parseColor("#D9B77C")
        }
        val reminder = NotificationChannel(
            CH_REMINDER, S.reminderBefore[l].substringBefore(" ("), NotificationManager.IMPORTANCE_HIGH
        )
        val info = NotificationChannel(
            CH_INFO, S.afterSalah[l], NotificationManager.IMPORTANCE_DEFAULT
        ).apply { setSound(null, null) }
        nm.createNotificationChannels(listOf(adhan, reminder, info))
    }

    fun dhikrIntent(ctx: Context, prayer: Int): PendingIntent {
        val i = Intent(ctx, DhikrActivity::class.java)
            .putExtra(DhikrActivity.EXTRA_PRAYER, prayer)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            ctx, 200 + prayer, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun builder(ctx: Context, channel: String): Notification.Builder =
        Notification.Builder(ctx, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.parseColor("#D9B77C"))

    /** Nach dem Adhan: dezente Benachrichtigung mit Link zu den Adhkar. */
    fun showAfterAdhan(ctx: Context, prayer: Int) {
        ensureChannels(ctx)
        val l = Prefs(ctx).lang
        val n = builder(ctx, CH_INFO)
            .setContentTitle(S.timeFor[l].format(S.prayerNames[prayer][l]))
            .setContentText(S.tapForAdhkar[l])
            .setContentIntent(dhikrIntent(ctx, prayer))
            .setAutoCancel(true)
            .build()
        notify(ctx, ID_AFTER, n)
    }

    /** Fallback, falls der Dienst nicht starten darf: Benachrichtigung mit Ton. */
    fun showPrayerWithSound(ctx: Context, prayer: Int) {
        ensureChannels(ctx)
        val l = Prefs(ctx).lang
        val n = builder(ctx, CH_REMINDER)
            .setContentTitle(S.timeFor[l].format(S.prayerNames[prayer][l]))
            .setContentText(S.tapForAdhkar[l])
            .setContentIntent(dhikrIntent(ctx, prayer))
            .setCategory(Notification.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        notify(ctx, ID_AFTER, n)
    }

    fun showReminder(ctx: Context, prayer: Int) {
        ensureChannels(ctx)
        val prefs = Prefs(ctx)
        val l = prefs.lang
        val n = builder(ctx, CH_REMINDER)
            .setContentTitle(S.reminderIn[l].format(S.prayerNames[prayer][l], prefs.reminderMinutes))
            .setContentIntent(mainIntent(ctx))
            .setCategory(Notification.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()
        notify(ctx, ID_REMINDER, n)
    }

    fun mainIntent(ctx: Context): PendingIntent = PendingIntent.getActivity(
        ctx, 300, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun notify(ctx: Context, id: Int, n: Notification) {
        try {
            ctx.getSystemService(NotificationManager::class.java).notify(id, n)
        } catch (e: SecurityException) {
            // Keine Benachrichtigungsberechtigung – ignorieren.
        }
    }
}
