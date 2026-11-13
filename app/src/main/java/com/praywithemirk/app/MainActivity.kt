package com.praywithemirk.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Hauptbildschirm:
 *  Kopfzeile  – Ort & Datum links, Uhrzeit rechts auf gleicher Höhe
 *  Hauptteil  – Ring (nächstes Gebet, Gebetszeit, Countdown) links, Tagesimpuls rechts
 *  darunter   – Gebetszeiten des Tages und Zugang zu den Adhkar
 */
class MainActivity : Activity() {

    private lateinit var prefs: Prefs
    private val handler = Handler(Looper.getMainLooper())
    private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

    private lateinit var placeView: TextView
    private lateinit var dateView: TextView
    private lateinit var hijriView: TextView
    private lateinit var clockView: TextView
    private lateinit var ring: RingView
    private lateinit var nextName: TextView
    private lateinit var nextArabic: TextView
    private lateinit var bigValue: TextView
    private lateinit var smallValue: TextView
    private lateinit var listBox: LinearLayout
    private lateinit var impulseBox: LinearLayout
    private lateinit var bannerBox: LinearLayout

    private var builtKey: String = ""
    private var wide = false
    private var ringDp = 160f
    private var fs = 1.3f
    private var prevEvent: PrayerEvent? = null
    private var nextEvent: PrayerEvent? = null
    private var impulseKey: String = ""

    private val tick = object : Runnable {
        override fun run() {
            onTick()
            val delay = 1000L - System.currentTimeMillis() % 1000L
            handler.postDelayed(this, delay + 5)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        Notifications.ensureChannels(this)
        buildUi()
        askInitialPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (builtKey != uiKey()) buildUi()
        if (prefs.keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        refreshAll()
        AlarmScheduler.reschedule(this)
        if (prefs.autoLocation && prefs.hasLocation && LocationHelper.hasPermission(this)) autoLocate()
        CustomSources.refreshStale(this)
        if (prefs.useMosque) MosqueCalendar.refreshIfStale(this)
        handler.removeCallbacks(tick)
        handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        buildUi()
        refreshAll()
    }

    private fun uiKey() = "${prefs.lang.id}|${prefs.theme.id}|${prefs.style.id}|${resources.configuration.screenWidthDp}|${prefs.ringBig}|${prefs.headerClock}|${prefs.clockSize}|${prefs.mainScale}|${resources.configuration.screenHeightDp}"

    // ---------------------------------------------------------------- Aufbau

    private fun buildUi() {
        val l = prefs.lang
        C.applyTo(this, prefs.theme)
        builtKey = uiKey()
        impulseKey = ""

        val screenDp = resources.configuration.screenWidthDp.toFloat()
        val screenH = resources.configuration.screenHeightDp.toFloat()
        wide = screenDp >= 600f
        fs = prefs.mainFactor
        val avail = screenDp - 40f
        // Ring so groß wie möglich: im Querformat/Tablet nach Bildschirmhöhe, im Hochformat gut die Hälfte der Breite
        ringDp = if (wide) minOf(screenH * 0.62f, avail * 0.46f) else avail * 0.54f

        val root = FrameLayout(this)
        root.addView(PatternBackground(this), FrameLayout.LayoutParams(-1, -1))

        val scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false; isFillViewport = true; fitsSystemWindows = true }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), dp(32))
        }
        scroll.addView(col)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        // ---- Hauptteil: Ring ganz oben links; rechts Ort/Datum + Uhrzeit, darunter der Impuls
        val main = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.TOP }

        val ringFrame = FrameLayout(this)
        ring = RingView(this)
        ringFrame.addView(ring, FrameLayout.LayoutParams(-1, -1))
        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val pad = dp(ringDp * 0.09f)
            setPadding(pad, pad, pad, pad)
        }
        val k = ringDp / 160f // Skalierung der Schrift mit der Ringgröße
        nextName = text("", 19f * k, C.gold, Fonts.bold, Gravity.CENTER).apply { maxLines = 1 }
        nextArabic = text("", 14f * k, C.muted, Fonts.serif, Gravity.CENTER).apply { maxLines = 1 }
        // großer Wert (Standard: Countdown) – passt sich automatisch der Breite an
        bigValue = text("", 40f * k, C.cream, Fonts.medium, Gravity.CENTER).apply {
            includeFontPadding = false
            fontFeatureSettings = "tnum"
            maxLines = 1
            setAutoSizeTextTypeUniformWithConfiguration(10, (48f * k).toInt().coerceAtLeast(12), 1, android.util.TypedValue.COMPLEX_UNIT_SP)
        }
        smallValue = text("", 17f * k, C.cream, Fonts.medium, Gravity.CENTER).apply { fontFeatureSettings = "tnum"; maxLines = 1 }
        center.addView(nextName)
        center.addView(nextArabic)
        center.addView(bigValue, LinearLayout.LayoutParams(-1, dp(54 * k)).apply { topMargin = dp(2 * k) })
        center.addView(smallValue, lp(top = dp(4 * k)))
        ringFrame.addView(center, FrameLayout.LayoutParams(-1, -1, Gravity.CENTER))
        main.addView(ringFrame, LinearLayout.LayoutParams(dp(ringDp), dp(ringDp)))

        val right = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Kopfzeile rechts: Ort/Datum (bündig mit dem Impuls-Rahmen) und daneben die Uhrzeit
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.TOP }
        val ts = (if (wide) 1.25f else 0.85f) * fs
        placeView = text("", 13f * ts, C.gold, Fonts.bold).apply { letterSpacing = 0.05f; maxLines = 2 }
        dateView = text("", 13f * ts, C.cream, Fonts.regular).apply { maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END }
        hijriView = text("", 12f * ts, C.muted, Fonts.regular).apply { maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END }
        // Ort links neben der Uhrzeit; Datum und Hijri-Datum darunter über die ganze Breite
        header.addView(placeView, LinearLayout.LayoutParams(0, -2, 1f))

        val clockSizes = if (wide) floatArrayOf(40f, 56f, 76f) else floatArrayOf(24f, 30f, 38f)
        clockView = text("", clockSizes[prefs.clockSize.coerceIn(0, 2)] * fs.coerceAtMost(1.3f), C.cream, Fonts.medium, Gravity.END).apply {
            includeFontPadding = false
            fontFeatureSettings = "tnum"
            maxLines = 1
            visibility = if (prefs.headerClock) View.VISIBLE else View.GONE
        }
        header.addView(clockView, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(6) })
        right.addView(header, lp(top = dp(6)))
        right.addView(dateView, lp(top = dp(2)))
        right.addView(hijriView)

        impulseBox = card(if (wide) 20 else 12)
        right.addView(impulseBox, lp(top = dp(10)))
        main.addView(right, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(if (wide) 20 else 10) })
        col.addView(main)

        bannerBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(bannerBox, lp(top = dp(4)))

        // ---- Gebetszeiten
        listBox = if (wide) LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL } else card(8)
        col.addView(listBox, lp(top = dp(16)))

        // ---- Schnellzugriff: Adhkar, Qibla, Anleitung
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun action(symbol: String, label: String, onClick: () -> Unit) = card(12).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(text(symbol, 20f * fs, C.gold, Fonts.regular, Gravity.CENTER))
            addView(text(label, 13f * fs, C.cream, Fonts.medium, Gravity.CENTER).apply { maxLines = 2 }, lp(top = dp(4)))
            clickableRipple(dp(20).toFloat())
            setOnClickListener { onClick() }
        }
        val nowSalah = { Schedule.currentSalah(prefs, ZonedDateTime.now(ZoneId.systemDefault())) }
        actions.addView(action("✦", S.adhkarShort[l]) {
            startActivity(Intent(this, DhikrActivity::class.java).putExtra(DhikrActivity.EXTRA_PRAYER, nowSalah()))
        }, LinearLayout.LayoutParams(0, -1, 1f))
        actions.addView(action("◈", S.qiblaShort[l]) {
            startActivity(Intent(this, QiblaActivity::class.java))
        }, LinearLayout.LayoutParams(0, -1, 1f).apply { leftMargin = dp(10) })
        actions.addView(action("❖", S.guideShort[l]) {
            val n = nextEvent?.prayer ?: P.FAJR
            startActivity(Intent(this, GuideActivity::class.java).putExtra(GuideActivity.EXTRA_PRAYER, if (n == P.SUNRISE) P.DHUHR else n))
        }, LinearLayout.LayoutParams(0, -1, 1f).apply { leftMargin = dp(10) })
        val settingsTile = card(12).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            addView(ImageView(this@MainActivity).apply {
                setImageResource(R.drawable.ic_settings)
                setColorFilter(C.gold)
            }, LinearLayout.LayoutParams(dp(24), dp(24)).apply { topMargin = dp(2) })
            addView(text(S.settings[l], 13f, C.cream, Fonts.regular, Gravity.CENTER).apply { maxLines = 2 }, lp(top = dp(4)))
            contentDescription = S.settings[l]
            clickableRipple(dp(20).toFloat())
            setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
        }
        actions.addView(settingsTile, LinearLayout.LayoutParams(0, -1, 1f).apply { leftMargin = dp(10) })
        col.addView(actions, lp(top = dp(14)))

        setContentView(root)
    }

    // ---------------------------------------------------------------- Aktualisieren

    private fun refreshAll() {
        val l = prefs.lang
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        prevEvent = Schedule.previous(prefs, now)
        nextEvent = Schedule.next(prefs, now)

        // auf schmalen Bildschirmen nur die Stadt, damit neben der Uhr genug Platz bleibt
        val place = if (wide) prefs.place else prefs.place.substringBefore(",").trim()
        val mosque = if (prefs.useMosque) MosqueCalendar.load(this)?.name else null
        placeView.text = (mosque ?: place.ifBlank { "%.2f°, %.2f°".format(prefs.lat, prefs.lng) }).uppercase()
        dateView.text = S.gregorian(now.toLocalDate(), l)
        hijriView.text = S.hijri(now.toLocalDate(), prefs.hijriOffset, l)

        buildBanners()
        buildList(now)
        buildImpulse(now)
        onTick()
    }

    private fun buildBanners() {
        val l = prefs.lang
        bannerBox.removeAllViews()
        if (!prefs.hasLocation) {
            bannerBox.addView(banner(S.noLocation[l]) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }, lp(top = dp(8)))
        }
        if (!AlarmScheduler.canScheduleExact(this) && Build.VERSION.SDK_INT >= 31) {
            bannerBox.addView(banner(S.exactAlarmInfo[l]) {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                )
            }, lp(top = dp(8)))
        }
    }

    private fun banner(msg: String, onClick: () -> Unit): View =
        text(msg, 13f, C.cream, Fonts.regular).apply {
            background = roundRect(alpha(C.gold, 42), dp(14).toFloat(), C.gold, dp(1))
            setPadding(dp(14), dp(10), dp(14), dp(10))
            clickableRipple(dp(14).toFloat())
            setOnClickListener { onClick() }
        }

    private fun bellView(i: Int): ImageView {
        val l = prefs.lang
        val bell = ImageView(this).apply { setPadding(dp(8), dp(6), dp(8), dp(6)) }
        if (i == P.SUNRISE) {
            bell.visibility = View.INVISIBLE
            return bell
        }
        fun paint() {
            val on = prefs.adhanEnabled(i)
            bell.setImageResource(if (on) R.drawable.ic_bell else R.drawable.ic_bell_off)
            bell.setColorFilter(if (on) C.gold else C.faint)
            bell.contentDescription = if (on) S.adhanOn[l] else S.adhanOff[l]
        }
        paint()
        bell.setOnClickListener {
            prefs.setAdhanEnabled(i, !prefs.adhanEnabled(i))
            paint()
            AlarmScheduler.reschedule(this)
        }
        return bell
    }

    private fun buildList(now: ZonedDateTime) {
        val l = prefs.lang
        listBox.removeAllViews()
        val times = prefs.timesFor(now.toLocalDate(), now.zone)
        val current = prevEvent?.takeIf { it.time.toLocalDate() == now.toLocalDate() }?.prayer
        val next = nextEvent?.takeIf { it.time.toLocalDate() == now.toLocalDate() }?.prayer

        for (i in P.ALL) {
            val isCurrent = i == current
            val nameColor = when {
                isCurrent -> C.gold
                i == P.SUNRISE -> C.muted
                else -> C.cream
            }
            if (wide) {
                // Kacheln nebeneinander (Tablet / Querformat)
                val tile = if (C.style == AppStyle.CLASSIC) LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    setPadding(dp(6), dp(12), dp(6), dp(6))
                    background = roundRect(
                        if (isCurrent) C.goldSoft else C.card, dp(18).toFloat(),
                        if (i == next) C.gold else C.cardStroke, dp(1)
                    )
                } else card(6).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    if (isCurrent) foreground = rowHighlight().also { it.alpha = 255 }
                }
                tile.addView(text(S.prayerNames[i][l], 16f * fs, nameColor, Fonts.bold, Gravity.CENTER).apply { maxLines = 1 })
                tile.addView(text(S.prayerArabic[i], 14f * fs, C.muted, Fonts.serif, Gravity.CENTER))
                tile.addView(text(times[i].format(hhmm), 30f * fs, nameColor, Fonts.medium, Gravity.CENTER).apply { fontFeatureSettings = "tnum"; maxLines = 1 })
                tile.addView(bellView(i), LinearLayout.LayoutParams(dp(36), dp(32)))
                if (i != P.SUNRISE) tile.setOnClickListener { openGuide(i) }
                listBox.addView(tile, LinearLayout.LayoutParams(0, -2, 1f).apply { if (i > 0) leftMargin = dp(8) })
            } else {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(14), dp(10), dp(6), dp(10))
                    if (isCurrent) background = rowHighlight()
                }
                val names = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                names.addView(text(S.prayerNames[i][l], 16f * fs, nameColor, if (isCurrent) Fonts.bold else Fonts.medium).apply { maxLines = 1 })
                names.addView(text(S.prayerArabic[i], 14f * fs, C.muted, Fonts.serif), LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(10) })
                if (i == next) names.addView(text("•", 18f, C.gold, Fonts.medium), LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(8) })
                row.addView(names, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(text(times[i].format(hhmm), 20f * fs, nameColor, Fonts.medium).apply { fontFeatureSettings = "tnum" })
                row.addView(bellView(i), LinearLayout.LayoutParams(dp(40), dp(36)))
                if (i != P.SUNRISE) row.setOnClickListener { openGuide(i) }
                listBox.addView(row)
            }
        }
    }

    private fun buildImpulse(now: ZonedDateTime) {
        val l = prefs.lang
        val times = prefs.timesFor(now.toLocalDate(), now.zone)
        val (evening, item) = DailyContent.current(now, times[P.FAJR], times[P.MAGHRIB], { prefs.allowed(it.source) }, CustomSources.enabledItems(this))
        val key = "${l.id}|${prefs.showTranslit}|$evening|${item?.arabic?.hashCode()}|${item?.meaning?.de?.hashCode()}"
        if (key == impulseKey) return
        impulseKey = key

        // Wanduhr/Tablet: Schrift wächst mit dem Platz; Handy hochkant: etwas kompakter (wenig Breite neben dem Ring)
        val s = if (wide) (ringDp / 260f).coerceIn(1.2f, 2.0f) * fs.coerceAtMost(1.3f) else 0.85f * fs.coerceAtMost(1.2f)
        impulseBox.removeAllViews()
        impulseBox.addView(
            text((if (evening) S.eveningImpulse[l] else S.morningImpulse[l]).uppercase(), 10f * s, C.gold, Fonts.bold)
                .apply { letterSpacing = 0.14f }
        )
        if (item == null) {
            impulseBox.addView(text(S.allSourcesOff[l], 14f * s, C.muted, Fonts.light), lp(top = dp(10)))
            return
        }
        impulseBox.addView(
            text(item.title[l], 15f * s, C.cream, Fonts.bold),
            lp(top = dp(6))
        )
        if (item.arabic.isNotBlank()) impulseBox.addView(arabic(item.arabic, 21f * s), lp(top = dp(8)))
        if (prefs.showTranslit && item.translit != null) {
            impulseBox.addView(text(item.translit, 13f * s, C.teal, Fonts.italic), lp(top = dp(8)))
        }
        impulseBox.addView(divider())
        impulseBox.addView(text(item.meaning[l], 15f * s, C.cream, Fonts.regular))
        impulseBox.addView(text("— " + item.source, 11f * s, C.muted, Fonts.medium, Gravity.END), lp(top = dp(8)))
    }

    private fun onTick() {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val next = nextEvent
        val prev = prevEvent
        if (next == null || prev == null || !now.isBefore(next.time)) {
            refreshAll(); return
        }
        // Uhr oben (optional mit kleinen Sekunden)
        val hm = now.format(hhmm)
        if (prefs.showSeconds) {
            val sec = ":%02d".format(now.second)
            val sp = SpannableString(hm + sec)
            sp.setSpan(RelativeSizeSpan(0.42f), hm.length, hm.length + sec.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sp.setSpan(ForegroundColorSpan(C.muted), hm.length, hm.length + sec.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            clockView.text = sp
        } else clockView.text = hm

        val l = prefs.lang
        nextName.text = S.prayerNames[next.prayer][l]
        nextArabic.text = S.prayerArabic[next.prayer]
        val s = Duration.between(now, next.time).seconds
        val cd = if (s >= 3600) "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
        else "%02d:%02d".format(s / 60, s % 60)
        val prayerTime = next.time.format(hhmm)
        when (prefs.ringBig) {
            1 -> { bigValue.text = prayerTime; smallValue.text = "−$cd" }
            2 -> { bigValue.text = hm; smallValue.text = "$prayerTime · −$cd" }
            else -> { bigValue.text = cd; smallValue.text = S.at[l].format(prayerTime) }
        }

        val total = Duration.between(prev.time, next.time).seconds.coerceAtLeast(1)
        val done = Duration.between(prev.time, now).seconds
        ring.progress = done.toFloat() / total

        if (now.second == 0) {
            dateView.text = S.gregorian(now.toLocalDate(), l)
            hijriView.text = S.hijri(now.toLocalDate(), prefs.hijriOffset, l)
            buildImpulse(now)
        }
    }

    private fun openGuide(prayer: Int) {
        startActivity(Intent(this, GuideActivity::class.java).putExtra(GuideActivity.EXTRA_PRAYER, prayer))
    }

    // ---------------------------------------------------------------- Berechtigungen & Standort

    private fun askInitialPermissions() {
        val wanted = mutableListOf<String>()
        if (!prefs.hasLocation && !LocationHelper.hasPermission(this)) wanted.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) wanted.add(Manifest.permission.POST_NOTIFICATIONS)
        if (wanted.isNotEmpty()) requestPermissions(wanted.toTypedArray(), 1)
        else if (!prefs.hasLocation) autoLocate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (LocationHelper.hasPermission(this) && !prefs.hasLocation) autoLocate()
    }

    private fun autoLocate() {
        LocationHelper.locate(this) { loc ->
            if (loc != null && prefs.autoLocation) {
                val old = android.location.Location("old").apply { latitude = prefs.lat; longitude = prefs.lng }
                if (prefs.hasLocation && prefs.place.isNotBlank() && old.distanceTo(loc) < 3000f) return@locate
                prefs.lat = loc.latitude
                prefs.lng = loc.longitude
                refreshAll()
                AlarmScheduler.reschedule(this)
                LocationHelper.placeName(this, loc.latitude, loc.longitude) { name ->
                    if (name != null) { prefs.place = name; refreshAll() }
                }
            }
        }
    }
}
