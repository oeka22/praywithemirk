package com.praywithemirk.app

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager

/**
 * Spielt den Gebetsruf als Vordergrund-Dienst ab (auch bei gesperrtem Bildschirm).
 * Ohne eigene Audiodatei wird der Standard-Weckton ca. 30 Sekunden lang wiederholt.
 */
class AdhanService : Service() {

    companion object {
        const val ACTION_PLAY = "com.praywithemirk.app.action.PLAY"
        const val ACTION_STOP = "com.praywithemirk.app.action.STOP"

        /** true, solange ein Gebetsruf läuft (für den Adhan-Bildschirm). */
        @Volatile var playing = false
        private const val MAX_DURATION_MS = 7 * 60_000L
        private const val DEFAULT_TONE_MS = 30_000L

        fun start(ctx: Context, prayer: Int, fromAlarm: Boolean) {
            val i = Intent(ctx, AdhanService::class.java)
                .setAction(ACTION_PLAY)
                .putExtra(AlarmScheduler.EXTRA_PRAYER, prayer)
            try {
                ctx.startForegroundService(i)
            } catch (e: Exception) {
                // z. B. ForegroundServiceStartNotAllowedException, wenn kein exakter Alarm erlaubt ist
                if (fromAlarm) Notifications.showPrayerWithSound(ctx, prayer)
            }
        }

        fun stop(ctx: Context) {
            ctx.startService(Intent(ctx, AdhanService::class.java).setAction(ACTION_STOP))
        }
    }

    private var player: MediaPlayer? = null
    private var prayer: Int = P.DHUHR
    private var focusRequest: AudioFocusRequest? = null
    private var savedAlarmVolume = -1
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { finish() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            finish()
            return START_NOT_STICKY
        }
        prayer = intent?.getIntExtra(AlarmScheduler.EXTRA_PRAYER, P.DHUHR) ?: P.DHUHR
        finished = false
        playing = true
        Notifications.ensureChannels(this)
        val n = buildNotification()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(Notifications.ID_ADHAN, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(Notifications.ID_ADHAN, n)
        }
        play()
        // Ist die App gerade sichtbar (z. B. als Wanduhr), den Adhan-Bildschirm direkt öffnen.
        if (App.visibleActivities > 0) {
            try { startActivity(adhanScreenIntent()) } catch (_: Exception) {}
        }
        return START_NOT_STICKY
    }

    private fun adhanScreenIntent(): Intent =
        Intent(this, AdhanActivity::class.java)
            .putExtra(AdhanActivity.EXTRA_PRAYER, prayer)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    private fun buildNotification(): Notification {
        val l = Prefs(this).lang
        val stopPi = PendingIntent.getService(
            this, 1,
            Intent(this, AdhanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = Notification.Action.Builder(null, S.stop[l], stopPi).build()
        val screenPi = PendingIntent.getActivity(
            this, 2, adhanScreenIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notifications.builder(this, Notifications.CH_ADHAN)
            .setContentTitle(S.timeFor[l].format(S.prayerNames[prayer][l]) + "  ·  " + S.prayerArabic[prayer])
            .setContentText(S.adhanPlaying[l])
            .setContentIntent(screenPi)
            .setFullScreenIntent(screenPi, true)
            .setDeleteIntent(stopPi)
            .addAction(stopAction)
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .build()
    }

    private fun play() {
        handler.removeCallbacksAndMessages(null)
        stopPlayer()
        val prefs = Prefs(this)
        val custom = prefs.soundFor(prayer)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        requestFocus(attrs)
        applyVolume(prefs.volume(prayer))

        val ok = custom != null && tryPlay(Uri.parse(custom), attrs, loop = false)
        if (!ok) {
            val def = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (def == null || !tryPlay(def, attrs, loop = true)) {
                finish(); return
            }
            handler.postDelayed(stopRunnable, DEFAULT_TONE_MS)
        }
        handler.postDelayed(stopRunnable, MAX_DURATION_MS)
    }

    private fun tryPlay(uri: Uri, attrs: AudioAttributes, loop: Boolean): Boolean = try {
        val mp = MediaPlayer()
        mp.setAudioAttributes(attrs)
        mp.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mp.setDataSource(applicationContext, uri)
        mp.isLooping = loop
        mp.setOnCompletionListener { finish() }
        mp.setOnErrorListener { _, _, _ -> finish(); true }
        mp.prepare()
        mp.start()
        player = mp
        true
    } catch (e: Exception) {
        false
    }

    /** Setzt die Wecker-Lautstärke für die Dauer des Gebetsrufs auf den gewünschten Anteil. */
    private fun applyVolume(percent: Int) {
        try {
            val am = getSystemService(AudioManager::class.java)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            if (savedAlarmVolume < 0) savedAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
            val target = if (percent <= 0) 0 else maxOf(1, Math.round(max * percent / 100f))
            am.setStreamVolume(AudioManager.STREAM_ALARM, target, 0)
            player?.setVolume(if (percent <= 0) 0f else 1f, if (percent <= 0) 0f else 1f)
        } catch (_: Exception) {}
    }

    private fun restoreVolume() {
        if (savedAlarmVolume < 0) return
        try { getSystemService(AudioManager::class.java).setStreamVolume(AudioManager.STREAM_ALARM, savedAlarmVolume, 0) } catch (_: Exception) {}
        savedAlarmVolume = -1
    }

    private fun requestFocus(attrs: AudioAttributes) {
        val am = getSystemService(AudioManager::class.java)
        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attrs)
            .build()
        focusRequest = req
        am.requestAudioFocus(req)
    }

    private fun stopPlayer() {
        player?.let {
            try { it.stop() } catch (_: Exception) {}
            it.release()
        }
        player = null
    }

    private var finished = false

    private fun finish() {
        if (finished) return
        finished = true
        playing = false
        sendBroadcast(Intent(AdhanActivity.ACTION_FINISHED).setPackage(packageName))
        handler.removeCallbacksAndMessages(null)
        stopPlayer()
        restoreVolume()
        focusRequest?.let { getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        Notifications.showAfterAdhan(this, prayer)
        stopSelf()
    }

    override fun onDestroy() {
        playing = false
        restoreVolume()
        handler.removeCallbacksAndMessages(null)
        stopPlayer()
        super.onDestroy()
    }
}
