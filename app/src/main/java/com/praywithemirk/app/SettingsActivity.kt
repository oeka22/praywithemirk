package com.praywithemirk.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.time.format.DateTimeFormatter

class SettingsActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var col: LinearLayout
    private var scrollY = 0
    private lateinit var scroll: ScrollView

    private val reqAdhan = 11
    private val reqPrayerBase = 20
    private val reqMicRecord = 15
    private val reqMicClap = 16
    private val reqLocation = 13

    private val goldTint get() = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
        intArrayOf(C.gold, C.faint)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        C.applyTo(this, prefs.theme)
        val root = FrameLayout(this)
        root.addView(PatternBackground(this), FrameLayout.LayoutParams(-1, -1))
        scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false; fitsSystemWindows = true }
        col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(48))
        }
        scroll.addView(col)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    /** Baut die gesamte Seite neu auf (einfach und robust). */
    private fun render() {
        scrollY = scroll.scrollY
        col.removeAllViews()
        val l = prefs.lang

        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(text("‹", 32f, C.gold, Fonts.light).apply {
            setPadding(dp(4), 0, dp(16), dp(4)); setOnClickListener { finish() }
        })
        head.addView(text(S.settings[l], 22f, C.cream, Fonts.light))
        col.addView(head)

        // ---------------- Sprache & Anzeige
        section(S.language[l])
        col.addView(card().apply {
            addView(segmented(Lang.entries.map { it.label }, Lang.entries.indexOf(l)) { idx ->
                prefs.lang = Lang.entries[idx]
                Notifications.ensureChannels(this@SettingsActivity)
                render()
            })
            addView(switchRow(S.showTranslit[prefs.lang], prefs.showTranslit) { prefs.showTranslit = it }, lp(top = dp(12)))
            addView(switchRow(S.keepScreenOn[prefs.lang], prefs.keepScreenOn) { prefs.keepScreenOn = it })
            addView(stepper(S.hijriOffset[l], prefs.hijriOffset, -2, 2, 1) { prefs.hijriOffset = it }, lp(top = dp(6)))
        }, lp(top = dp(8)))

        // ---------------- Anzeige
        section(S.display[l])
        col.addView(card().apply {
            addView(label(S.ringBig[l]))
            addView(radio(S.ringOptions.map { it[l] }, prefs.ringBig) { prefs.ringBig = it; render() })
            addView(switchRow(S.headerClock[l], prefs.headerClock) { prefs.headerClock = it }, lp(top = dp(10)))
            addView(switchRow(S.showSeconds[l], prefs.showSeconds) { prefs.showSeconds = it })
            addView(label(S.mainScale[l]), lp(top = dp(10)))
            addView(segmented(S.mainScales.map { it[l] }, prefs.mainScale) { prefs.mainScale = it; render() }, lp(top = dp(6)))
            addView(label(S.clockSize[l]), lp(top = dp(10)))
            addView(segmented(S.sizes.map { it[l] }, prefs.clockSize) { prefs.clockSize = it; render() }, lp(top = dp(6)))
        }, lp(top = dp(8)))

        // ---------------- Design
        section(S.design[l])
        col.addView(card(14).apply {
            val grid = LinearLayout(this@SettingsActivity).apply { orientation = LinearLayout.HORIZONTAL }
            AppTheme.entries.forEachIndexed { idx, t -> grid.addView(themeTile(t), LinearLayout.LayoutParams(0, -2, 1f).apply { if (idx > 0) leftMargin = dp(8) }) }
            addView(grid)
            addView(text(prefs.theme.title[l], 14f, C.gold, Fonts.medium, Gravity.CENTER), lp(top = dp(10)))
            addView(label(S.styleLabel[l]), lp(top = dp(14)))
            val st = AppStyle.entries
            for (rowIdx in 0..1) {
                addView(segmented(st.subList(rowIdx * 2, rowIdx * 2 + 2).map { it.title[l] },
                    st.indexOf(prefs.style) - rowIdx * 2) { idx ->
                    prefs.style = st[rowIdx * 2 + idx]; recreate()
                }, lp(top = dp(6)))
            }
        }, lp(top = dp(8)))

        // ---------------- Standort
        section(S.location[l])
        col.addView(card().apply {
            val coord = "%.4f°, %.4f°".format(prefs.lat, prefs.lng)
            addView(text(prefs.place.ifBlank { "—" }, 17f, C.cream, Fonts.regular))
            addView(text(coord, 13f, C.muted, Fonts.light))
            addView(switchRow(S.useGps[l], prefs.autoLocation) {
                prefs.autoLocation = it
                if (it) locateNow()
            }, lp(top = dp(8)))

            addView(label(S.searchCity[l]), lp(top = dp(12)))
            val row = LinearLayout(this@SettingsActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val input = edit(S.cityHint[l], InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
            row.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
            val results = LinearLayout(this@SettingsActivity).apply { orientation = LinearLayout.VERTICAL }
            row.addView(pill("🔍") { searchCity(input.text.toString(), results) }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(8) })
            addView(row, lp(top = dp(4)))
            addView(results)

            addView(label(S.manualCoords[l]), lp(top = dp(12)))
            val row2 = LinearLayout(this@SettingsActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val coords = edit("52.52, 13.405", InputType.TYPE_CLASS_TEXT)
            row2.addView(coords, LinearLayout.LayoutParams(0, -2, 1f))
            row2.addView(pill(S.save[l]) {
                val parts = coords.text.toString().split(",", ";", " ").mapNotNull { it.trim().toDoubleOrNull() }
                if (parts.size >= 2 && parts[0] in -90.0..90.0 && parts[1] in -180.0..180.0) {
                    setLocation(parts[0], parts[1], "", auto = false)
                }
            }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(8) })
            addView(row2, lp(top = dp(4)))
        }, lp(top = dp(8)))

        // ---------------- Quelle der Gebetszeiten: Berechnung oder Moschee-Kalender
        section(S.timeSource[l])
        col.addView(card().apply {
            addView(segmented(listOf(S.srcCalc[l], S.srcMosque[l]), if (prefs.useMosque) 1 else 0) { idx ->
                prefs.useMosque = idx == 1; changed()
            })
            if (prefs.useMosque) {
                addView(text(S.mosqueInfo[l], 13f, C.muted, Fonts.light), lp(top = dp(10)))
                val cal = MosqueCalendar.load(this@SettingsActivity)
                if (cal == null) addView(text(S.mosqueNone[l], 15f, C.gold, Fonts.medium), lp(top = dp(10)))
                else {
                    addView(text(S.mosqueCurrent[l].format(cal.name), 15f, C.gold, Fonts.medium), lp(top = dp(10)))
                    val d = java.time.Instant.ofEpochMilli(cal.fetchedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    addView(text(S.mosqueUpdated[l].format(d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) , 12f, C.muted, Fonts.light))
                    addView(text("↻ " + S.refresh[l], 13f, C.teal, Fonts.regular).apply {
                        setPadding(0, dp(6), 0, dp(4))
                        setOnClickListener { loadMosque(cal.url, cal.name) }
                    })
                }
                for (p in MosqueCalendar.presets) {
                    val sel = cal != null && cal.name == p.name
                    addView(LinearLayout(this@SettingsActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(14), dp(10), dp(14), dp(10))
                        background = roundRect(if (sel) C.goldSoft else Color.TRANSPARENT, dp(14).toFloat(), if (sel) C.gold else C.cardStroke, dp(1))
                        addView(text((if (sel) "✓  " else "🕌  ") + p.name, 15f, if (sel) C.gold else C.cream, Fonts.regular))
                        p.note?.let { addView(text(it[l], 12f, C.muted, Fonts.light), lp(top = dp(2))) }
                        setOnClickListener { loadMosque(p.url, p.name) }
                    }, lp(top = dp(8)))
                }
                addView(label(S.mosqueOther[l]), lp(top = dp(14)))
                addView(text(S.mosqueLinkHint[l], 12f, C.muted, Fonts.light))
                val row = LinearLayout(this@SettingsActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                val input = edit("https://mawaqit.net/…", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
                row.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(pill(S.ok[l]) {
                    val u = input.text.toString().trim().let { Regex("https://\\S+").find(it)?.value ?: it }
                    if (!MosqueCalendar.isMawaqitUrl(u)) Toast.makeText(this@SettingsActivity, S.mosqueOnlyMawaqit[l], Toast.LENGTH_LONG).show()
                    else loadMosque(u, null)
                }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(8) })
                addView(row, lp(top = dp(6)))
                addView(text(S.calcHiddenNote[l], 12f, C.faint, Fonts.italic), lp(top = dp(12)))
            }
        }, lp(top = dp(8)))

        // ---------------- Berechnung
        section(S.method[l])
        col.addView(card().apply {
            addView(radio(Method.entries.map { it.label }, Method.entries.indexOf(prefs.method)) {
                prefs.method = Method.entries[it]; changed()
            })
            addView(label(S.asrMadhhab[l]), lp(top = dp(14)))
            addView(radio(listOf(S.asrStandard[l], S.asrHanafi[l]), if (prefs.hanafiAsr) 1 else 0) {
                prefs.hanafiAsr = it == 1; changed()
            })
            addView(label(S.highLat[l]), lp(top = dp(14)))
            addView(radio(S.highLatNames.map { it[l] }, HighLatRule.entries.indexOf(prefs.highLat)) {
                prefs.highLat = HighLatRule.entries[it]; changed()
            })
            addView(label(S.offsets[l]), lp(top = dp(14)))
            for (i in P.ALL) {
                addView(stepper(S.prayerNames[i][l], prefs.offset(i), -30, 30, 1) {
                    prefs.setOffset(i, it); changed()
                })
            }
        }, lp(top = dp(8)))

        // ---------------- Gebetsruf
        section(S.adhanSection[l])
        col.addView(card().apply {
            addView(label(S.adhanFor[l]))
            for (i in P.SALAH) {
                addView(switchRow(S.prayerNames[i][l] + "   " + S.prayerArabic[i], prefs.adhanEnabled(i)) {
                    prefs.setAdhanEnabled(i, it); changed()
                })
            }
            addView(label(S.adhanSound[l]), lp(top = dp(14)))
            addView(text(S.audioHint[l] + " " + S.linkHint[l], 12f, C.muted, Fonts.light))
            addView(text(S.defaultForAll[l], 15f, C.gold, Fonts.medium), lp(top = dp(12)))
            addView(soundRow(-1, prefs.adhanName ?: S.defaultSound[l]), lp(top = dp(4)))
            addView(label(S.perPrayer[l]), lp(top = dp(16)))
            addView(text(S.volumeInfo[l], 12f, C.muted, Fonts.light))
            for (i in P.SALAH) {
                addView(text(S.prayerNames[i][l] + "   " + S.prayerArabic[i], 15f, C.gold, Fonts.medium), lp(top = dp(12)))
                addView(soundRow(i, prefs.prayerName(i) ?: S.useDefault[l]), lp(top = dp(4)))
                addView(stepper("🔊 " + S.volume[l] + " (%)", prefs.volume(i), 0, 100, 10) { v -> prefs.setVolume(i, v) }, lp(top = dp(4)))
            }

            addView(stepper(S.reminderBefore[l], prefs.reminderMinutes, 0, 60, 5) {
                prefs.reminderMinutes = it; changed()
            }, lp(top = dp(14)))

            addView(switchRow(S.clapSetting[l], prefs.clapToStop) { on ->
                if (on && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), reqMicClap)
                } else prefs.clapToStop = on
            }, lp(top = dp(10)))
            addView(text(S.clapInfo[l], 12f, C.muted, Fonts.light))
        }, lp(top = dp(8)))

        // ---------------- Eigene Aufnahmen
        section(S.recordings[l])
        col.addView(card().apply {
            addView(text(S.recordingsInfo[l], 13f, C.muted, Fonts.light))
            val recs = Recordings.list(this@SettingsActivity)
            if (recs.isEmpty()) addView(text(S.noRecordings[l], 14f, C.cream, Fonts.light), lp(top = dp(10)))
            recs.forEach { r ->
                val row = LinearLayout(this@SettingsActivity).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(8), 0, dp(4))
                }
                row.addView(text("🎙  " + r.name, 15f, C.cream, Fonts.regular).apply { maxLines = 2 }, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(pill("▶") { preview(r.uri) }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(6) })
                row.addView(pill("■") { preview(null) }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(6) })
                row.addView(pill("🗑") {
                    AlertDialog.Builder(this@SettingsActivity, dialogStyle())
                        .setTitle(S.deleteRecording[l])
                        .setMessage(r.name)
                        .setNegativeButton(S.cancel[l], null)
                        .setPositiveButton(S.delete[l]) { _, _ ->
                            preview(null); Recordings.delete(this@SettingsActivity, r); render()
                        }.show()
                }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(6) })
                addView(row)
            }
            addView(pill("●  " + S.newRecording[l], filled = true) { recordDialog(null) },
                lp(LinearLayout.LayoutParams.WRAP_CONTENT, top = dp(12)))
        }, lp(top = dp(8)))

        // ---------------- Quellen
        section(S.sources[l])
        col.addView(card().apply {
            addView(text(S.sourcesInfo[l], 13f, C.muted, Fonts.light))
            fun switchFor(on: Boolean, change: (Boolean) -> Unit) = Switch(this@SettingsActivity).apply {
                isChecked = on
                thumbTintList = goldTint
                trackTintList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(alpha(C.gold, 128), alpha(C.faint, 96))
                )
                setOnCheckedChangeListener { _: CompoundButton, c: Boolean -> change(c) }
            }
            fun confirmDelete(title: String, onYes: () -> Unit) {
                AlertDialog.Builder(this@SettingsActivity, dialogStyle())
                    .setTitle(S.deleteSource[l]).setMessage(title + "\n\n" + S.deleteSourceInfo[l])
                    .setNegativeButton(S.cancel[l], null)
                    .setPositiveButton(S.delete[l]) { _, _ -> onYes() }.show()
            }
            for (src in Src.entries.filter { !prefs.sourceDeleted(it) }) {
                val row = LinearLayout(this@SettingsActivity).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(10), 0, dp(4))
                }
                val txt = LinearLayout(this@SettingsActivity).apply { orientation = LinearLayout.VERTICAL }
                txt.addView(text(src.title[l], 15f, C.cream, Fonts.regular))
                txt.addView(text(src.info[l] + " · " + S.itemsCount[l].format(sourceCount(src)), 12f, C.muted, Fonts.light))
                row.addView(txt, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(switchFor(prefs.sourceEnabled(src)) { prefs.setSourceEnabled(src, it) })
                row.addView(text("🗑", 18f, C.muted, Fonts.regular, Gravity.CENTER).apply {
                    setPadding(dp(10), dp(6), dp(4), dp(6))
                    setOnClickListener { confirmDelete(src.title[l]) { prefs.setSourceDeleted(src, true); render() } }
                })
                addView(row)
            }
            for (cs in CustomSources.list(this@SettingsActivity)) {
                val row = LinearLayout(this@SettingsActivity).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(10), 0, dp(4))
                }
                val txt = LinearLayout(this@SettingsActivity).apply { orientation = LinearLayout.VERTICAL }
                txt.addView(text("🔗 " + cs.name, 15f, C.cream, Fonts.regular))
                txt.addView(text(S.ownSource[l] + " · " + S.itemsCount[l].format(cs.count), 12f, C.muted, Fonts.light))
                txt.addView(text(cs.url, 11f, C.faint, Fonts.light).apply { maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.MIDDLE })
                txt.addView(text("↻ " + S.refresh[l], 12f, C.teal, Fonts.regular).apply {
                    setPadding(0, dp(4), dp(8), dp(4))
                    setOnClickListener {
                        Toast.makeText(this@SettingsActivity, S.downloading[l], Toast.LENGTH_SHORT).show()
                        CustomSources.refresh(this@SettingsActivity, cs.id) { n ->
                            Toast.makeText(this@SettingsActivity,
                                if (n > 0) S.sourceLoaded[l].format(n) else S.sourceFailed[l], Toast.LENGTH_SHORT).show()
                            render()
                        }
                    }
                })
                row.addView(txt, LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(switchFor(cs.enabled) { CustomSources.setEnabled(this@SettingsActivity, cs.id, it) })
                row.addView(text("🗑", 18f, C.muted, Fonts.regular, Gravity.CENTER).apply {
                    setPadding(dp(10), dp(6), dp(4), dp(6))
                    setOnClickListener { confirmDelete(cs.name) { CustomSources.delete(this@SettingsActivity, cs.id); render() } }
                })
                addView(row)
            }
            addView(pill("＋  " + S.addSource[l], filled = true) { addSourceDialog() },
                lp(LinearLayout.LayoutParams.WRAP_CONTENT, top = dp(14)))
            val deleted = Src.entries.count { prefs.sourceDeleted(it) }
            if (deleted > 0) addView(pill("↺  " + S.restoreSources[l] + " ($deleted)") {
                Src.entries.forEach { prefs.setSourceDeleted(it, false) }; render()
            }, lp(LinearLayout.LayoutParams.WRAP_CONTENT, top = dp(8)))
        }, lp(top = dp(8)))

        // ---------------- Berechtigungen
        section(S.permissions[l])
        col.addView(card().apply {
            if (Build.VERSION.SDK_INT >= 31) {
                val ok = AlarmScheduler.canScheduleExact(this@SettingsActivity)
                addView(text((if (ok) "✓  " else "•  ") + S.exactAlarmInfo[l], 14f, if (ok) C.teal else C.cream, Fonts.light))
                if (!ok) addView(pill(S.openSettings[l], filled = true) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                }, lp(LinearLayout.LayoutParams.WRAP_CONTENT, top = dp(8), bottom = dp(8)))
            }
            if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                addView(pill("🔔  " + S.openSettings[l]) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2)
                }, lp(LinearLayout.LayoutParams.WRAP_CONTENT, top = dp(8), bottom = dp(8)))
            }
            if (Build.VERSION.SDK_INT >= 34) {
                val nm = getSystemService(android.app.NotificationManager::class.java)
                val ok = nm.canUseFullScreenIntent()
                addView(text((if (ok) "✓  " else "•  ") + S.fullScreenInfo[l], 14f, if (ok) C.teal else C.cream, Fonts.light), lp(top = dp(8)))
                if (!ok) addView(pill(S.openSettings[l], filled = true) {
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName")))
                    } catch (_: Exception) {}
                }, lp(LinearLayout.LayoutParams.WRAP_CONTENT, top = dp(8), bottom = dp(8)))
            }
            addView(text(S.batteryInfo[l], 14f, C.cream, Fonts.light), lp(top = dp(8)))
            addView(pill(S.openSettings[l]) {
                try { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) {}
            }, lp(LinearLayout.LayoutParams.WRAP_CONTENT, top = dp(8)))
        }, lp(top = dp(8)))

        // Nächster Gebetsruf
        val next = AlarmScheduler.reschedule(this)
        if (next != null) {
            val f = DateTimeFormatter.ofPattern("EEE HH:mm", java.util.Locale.forLanguageTag(l.id))
            col.addView(
                text(S.nextAdhan[l].format(S.prayerNames[next.prayer][l] + " · " + next.time.format(f)), 13f, C.muted, Fonts.light, Gravity.CENTER),
                lp(top = dp(20))
            )
        }
        col.addView(text("Pray with Emir K. · 1.0", 11f, C.faint, Fonts.light, Gravity.CENTER), lp(top = dp(8)))

        scroll.post { scroll.scrollTo(0, scrollY) }
    }

    private fun changed() {
        AlarmScheduler.reschedule(this)
        render()
    }

    private fun loadMosque(url: String, name: String?) {
        val l = prefs.lang
        Toast.makeText(this, S.downloading[l], Toast.LENGTH_SHORT).show()
        MosqueCalendar.fetch(this, url, name) { n ->
            if (n == null) Toast.makeText(this, S.mosqueFailed[l], Toast.LENGTH_LONG).show()
            else {
                prefs.useMosque = true
                Toast.makeText(this, S.mosqueLoaded[l].format(n), Toast.LENGTH_SHORT).show()
            }
            changed()
        }
    }

    private fun addSourceDialog() {
        val l = prefs.lang
        val name = edit(S.sourceName[l], InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        val url = edit("https://…", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0)
            addView(text(S.sourceUrlHint[l], 12f, C.muted, Fonts.light))
            addView(name, lp(top = dp(10)))
            addView(url, lp(top = dp(8)))
        }
        AlertDialog.Builder(this, dialogStyle())
            .setTitle(S.addSource[l])
            .setView(ScrollView(this).apply { addView(wrap) })
            .setNegativeButton(S.cancel[l], null)
            .setPositiveButton(S.ok[l]) { _, _ ->
                val u = url.text.toString().trim()
                if (!u.startsWith("https://", true)) {
                    Toast.makeText(this, S.httpsOnly[l], Toast.LENGTH_LONG).show(); return@setPositiveButton
                }
                Toast.makeText(this, S.downloading[l], Toast.LENGTH_SHORT).show()
                CustomSources.add(this, name.text.toString().trim(), u) { n ->
                    Toast.makeText(this, if (n > 0) S.sourceLoaded[l].format(n) else S.sourceFailed[l], Toast.LENGTH_LONG).show()
                    render()
                }
            }.show()
    }

    /** Anzahl der Inhalte (Impulse + Adhkar), die eine Quelle verwenden. */
    private fun sourceCount(src: Src): Int {
        val all = DailyContent.quran.map { it.source } + DailyContent.hadith.map { it.source } +
            (Adhkar.atAdhan + P.SALAH.flatMap { Adhkar.afterSalah(it) }.distinct()).map { it.source }
        return all.count { src in Src.parse(it) }
    }

    /** Vorschau-Kachel für ein Design. */
    private fun themeTile(t: AppTheme): View {
        val sel = t == prefs.theme
        val p = t.p
        val tile = object : View(this) {
            private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            override fun onDraw(c: android.graphics.Canvas) {
                val w = width.toFloat(); val h = height.toFloat()
                val r = dp(14).toFloat()
                paint.shader = android.graphics.LinearGradient(0f, 0f, w * 0.3f, h,
                    Color.parseColor(p.bgTop), Color.parseColor(p.bgBottom), android.graphics.Shader.TileMode.CLAMP)
                c.drawRoundRect(0f, 0f, w, h, r, r, paint)
                paint.shader = null
                // kleiner Ring + Textzeilen als Miniatur
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = dp(2).toFloat()
                paint.color = Color.parseColor(p.accent)
                c.drawCircle(w * 0.32f, h * 0.42f, w * 0.18f, paint)
                paint.style = android.graphics.Paint.Style.FILL
                paint.color = Color.parseColor(p.text)
                c.drawRoundRect(w * 0.58f, h * 0.30f, w * 0.86f, h * 0.36f, 4f, 4f, paint)
                paint.color = Color.parseColor(p.muted)
                c.drawRoundRect(w * 0.58f, h * 0.44f, w * 0.80f, h * 0.49f, 4f, 4f, paint)
                paint.color = Color.parseColor(p.card)
                c.drawRoundRect(w * 0.12f, h * 0.72f, w * 0.88f, h * 0.86f, 6f, 6f, paint)
                if (sel) {
                    paint.style = android.graphics.Paint.Style.STROKE
                    paint.strokeWidth = dp(2.5f).toFloat()
                    paint.color = C.gold
                    c.drawRoundRect(2f, 2f, w - 2f, h - 2f, r, r, paint)
                    paint.style = android.graphics.Paint.Style.FILL
                }
            }
        }
        tile.contentDescription = t.title[prefs.lang]
        tile.setOnClickListener {
            if (t != prefs.theme) {
                prefs.theme = t
                recreate()
            }
        }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        box.addView(tile, LinearLayout.LayoutParams(-1, dp(64)))
        return box
    }

    // ---------------------------------------------------------------- Bausteine

    private fun section(title: String) {
        col.addView(text(title.uppercase(), 12f, C.gold, Fonts.medium).apply { letterSpacing = 0.16f }, lp(top = dp(24)))
    }

    private fun label(s: String): TextView = text(s, 13f, C.muted, Fonts.medium)

    private fun edit(hint: String, type: Int): EditText = EditText(this).apply {
        this.hint = hint
        inputType = type
        setTextColor(C.cream)
        setHintTextColor(C.faint)
        textSize = 15f
        background = roundRect(alpha(C.cream, 18), dp(12).toFloat(), C.cardStroke, dp(1))
        setPadding(dp(12), dp(10), dp(12), dp(10))
        isSingleLine = true
    }

    private fun switchRow(label: String, value: Boolean, onChange: (Boolean) -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))
        }
        row.addView(text(label, 15f, C.cream, Fonts.light), LinearLayout.LayoutParams(0, -2, 1f))
        val sw = Switch(this).apply {
            isChecked = value
            thumbTintList = goldTint
            trackTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(alpha(C.gold, 128), alpha(C.faint, 96))
            )
            setOnCheckedChangeListener { _: CompoundButton, c: Boolean -> onChange(c) }
        }
        row.addView(sw)
        return row
    }

    private fun radio(options: List<String>, selected: Int, onSelect: (Int) -> Unit): RadioGroup {
        val g = RadioGroup(this)
        options.forEachIndexed { i, s ->
            val rb = RadioButton(this).apply {
                id = View.generateViewId()
                text = s
                setTextColor(C.cream)
                textSize = 15f
                buttonTintList = goldTint
                isChecked = i == selected
                setPadding(dp(6), dp(6), 0, dp(6))
                setOnClickListener { if (i != selected) onSelect(i) }
            }
            g.addView(rb)
        }
        return g
    }

    private fun segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        options.forEachIndexed { i, s ->
            val sel = i == selected
            val v = text(s, 14f, if (sel) C.onAccent else C.cream, Fonts.medium, Gravity.CENTER).apply {
                background = roundRect(if (sel) C.gold else Color.TRANSPARENT, dp(16).toFloat(), if (sel) C.gold else C.cardStroke, dp(1))
                setPadding(dp(6), dp(10), dp(6), dp(10))
                setOnClickListener { if (!sel) onSelect(i) }
            }
            row.addView(v, LinearLayout.LayoutParams(0, -2, 1f).apply { if (i > 0) leftMargin = dp(8) })
        }
        return row
    }

    private fun stepper(label: String, value: Int, min: Int, max: Int, step: Int, onChange: (Int) -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        row.addView(text(label, 15f, C.cream, Fonts.light), LinearLayout.LayoutParams(0, -2, 1f))
        var v = value
        val valueView = text(if (v > 0 && min < 0) "+$v" else "$v", 16f, C.gold, Fonts.medium, Gravity.CENTER).apply {
            fontFeatureSettings = "tnum"
        }
        fun btn(sym: String, delta: Int) = text(sym, 20f, C.gold, Fonts.light, Gravity.CENTER).apply {
            background = roundRect(Color.TRANSPARENT, dp(18).toFloat(), C.cardStroke, dp(1))
            setOnClickListener {
                val nv = (v + delta).coerceIn(min, max)
                if (nv != v) {
                    v = nv
                    valueView.text = if (v > 0 && min < 0) "+$v" else "$v"
                    onChange(v)
                }
            }
        }
        row.addView(btn("−", -step), LinearLayout.LayoutParams(dp(36), dp(36)))
        row.addView(valueView, LinearLayout.LayoutParams(dp(48), -2))
        row.addView(btn("+", step), LinearLayout.LayoutParams(dp(36), dp(36)))
        return row
    }

    /** Zeile für einen Gebetsruf: [slot] = -1 für Standard, sonst Gebetsindex. */
    private fun soundRow(slot: Int, name: String): View {
        val l = prefs.lang
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        box.addView(text("♪  $name", 14f, C.cream, Fonts.regular).apply { maxLines = 2 })
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(pill(S.choose[l], filled = true) { chooseSound(slot) })
        row.addView(pill("▶") {
            AdhanService.start(this, if (slot < 0) P.DHUHR else slot, false)
        }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(6) })
        row.addView(pill("■") { AdhanService.stop(this) }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(6) })
        row.addView(pill("✕") {
            AdhanService.stop(this)
            setSound(slot, null, null)
        }, LinearLayout.LayoutParams(-2, -2).apply { leftMargin = dp(6) })
        val hs = android.widget.HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; addView(row) }
        box.addView(hs, lp(top = dp(6)))
        return box
    }

    private fun setSound(slot: Int, uri: String?, name: String?) {
        if (slot < 0) { prefs.adhanUri = uri; prefs.adhanName = name }
        else prefs.setPrayerSound(slot, uri, name)
        render()
    }

    /** Auswahl-Dialog: eigene Aufnahmen, Vorschläge, Datei, Link oder neue Aufnahme. */
    private fun chooseSound(slot: Int) {
        val l = prefs.lang
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), dp(8)) }
        var dialog: android.app.Dialog? = null
        fun item(label: String, action: () -> Unit) {
            wrap.addView(text(label, 15f, C.cream, Fonts.regular).apply {
                setPadding(dp(4), dp(11), dp(4), dp(11))
                clickableRipple(dp(10).toFloat())
                setOnClickListener { dialog?.dismiss(); action() }
            })
        }
        fun head(t: String) = wrap.addView(text(t.uppercase(), 11f, C.gold, Fonts.medium).apply { letterSpacing = 0.12f }, lp(top = dp(12)))

        head(S.recordings[l])
        val recs = Recordings.list(this)
        if (recs.isEmpty()) wrap.addView(text(S.noRecordings[l], 13f, C.muted, Fonts.light), lp(top = dp(4)))
        recs.forEach { r -> item("🎙  " + r.name) { setSound(slot, r.uri, "🎙 " + r.name) } }
        item("●  " + S.newRecording[l]) { recordDialog(slot) }

        head(S.presets[l])
        AdhanPresets.list.forEach { pr -> item("♪  " + pr.title) { startDownload(slot, pr.url, pr.title) } }

        head(S.more[l])
        item("📁  " + S.fileFromPhone[l]) { pickAudio(if (slot < 0) reqAdhan else reqPrayerBase + slot) }
        item("🔗  " + S.enterLink[l]) { askLink(slot) }
        item("⏰  " + S.defaultSound[l]) { setSound(slot, null, null) }

        dialog = AlertDialog.Builder(this, dialogStyle())
            .setTitle(S.choose[l])
            .setView(ScrollView(this).apply { addView(wrap) })
            .setNegativeButton(S.cancel[l], null)
            .create()
        dialog.show()
    }

    private fun dialogStyle() = if (C.light) android.R.style.Theme_Material_Light_Dialog_Alert else android.R.style.Theme_Material_Dialog_Alert

    private var pendingRecordSlot: Int? = null

    /** Aufnahme-Dialog (max. 3 Minuten). [assignSlot] = Gebet, dem die Aufnahme direkt zugewiesen wird (oder null). */
    private fun recordDialog(assignSlot: Int?) {
        val l = prefs.lang
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            pendingRecordSlot = assignSlot ?: Int.MIN_VALUE
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), reqMicRecord)
            return
        }
        val file = Recordings.newFile(this)
        var recorder: android.media.MediaRecorder? = null
        var startAt = 0L
        var recorded = false
        val handler = android.os.Handler(android.os.Looper.getMainLooper())

        val timer = text("0:00 / 3:00", 34f, C.cream, Fonts.light, Gravity.CENTER).apply { fontFeatureSettings = "tnum" }
        val status = text(S.recordHint[l], 13f, C.muted, Fonts.light, Gravity.CENTER)
        val name = edit(S.recordingName[l], InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES).apply { visibility = View.GONE }
        val btn = pill("●  " + S.startRecording[l], filled = true) {}
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(12), dp(20), dp(4))
            addView(timer); addView(status, lp(top = dp(4)))
            addView(btn, LinearLayout.LayoutParams(-2, -2).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(14) })
            addView(name, lp(top = dp(14)))
        }
        lateinit var dialog: AlertDialog
        val tick = object : Runnable {
            override fun run() {
                val ms = System.currentTimeMillis() - startAt
                val sec = (ms / 1000).toInt().coerceAtMost(Recordings.MAX_MS / 1000)
                timer.text = "%d:%02d / 3:00".format(sec / 60, sec % 60)
                if (recorder != null) handler.postDelayed(this, 250)
            }
        }
        fun stopRec() {
            val r = recorder ?: return
            recorder = null
            handler.removeCallbacks(tick)
            try { r.stop(); recorded = true } catch (_: Exception) { recorded = false; file.delete() }
            r.release()
            btn.text = "●  " + S.recordAgain[l]
            status.text = if (recorded) S.recordDone[l] else S.recordFailed[l]
            name.visibility = if (recorded) View.VISIBLE else View.GONE
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = recorded
        }
        btn.setOnClickListener {
            if (recorder != null) { stopRec(); return@setOnClickListener }
            try {
                file.delete()
                val r = Recordings.recorder(this, file)
                r.setOnInfoListener { _, what, _ ->
                    if (what == android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) stopRec()
                }
                r.start()
                recorder = r
                recorded = false
                startAt = System.currentTimeMillis()
                btn.text = "■  " + S.stopRecording[l]
                status.text = S.recording[l]
                name.visibility = View.GONE
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                handler.post(tick)
            } catch (e: Exception) {
                status.text = S.recordFailed[l]
            }
        }
        dialog = AlertDialog.Builder(this, dialogStyle())
            .setTitle(S.newRecording[l])
            .setView(wrap)
            .setNegativeButton(S.cancel[l]) { _, _ -> }
            .setPositiveButton(S.save[l]) { _, _ ->
                val rec = Recordings.add(this, file, name.text.toString().trim())
                if (assignSlot != null) setSound(assignSlot, rec.uri, "🎙 " + rec.name) else render()
            }
            .create()
        dialog.setOnDismissListener {
            if (recorder != null) { try { recorder?.stop() } catch (_: Exception) {}; recorder?.release(); recorder = null; file.delete() }
            handler.removeCallbacksAndMessages(null)
            if (Recordings.list(this).none { it.file == file }) file.delete()
        }
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
    }

    private var previewPlayer: android.media.MediaPlayer? = null

    private fun preview(uri: String?) {
        previewPlayer?.release(); previewPlayer = null
        if (uri == null) return
        try {
            previewPlayer = android.media.MediaPlayer().apply {
                setDataSource(this@SettingsActivity, Uri.parse(uri))
                setOnCompletionListener { it.release(); if (previewPlayer === it) previewPlayer = null }
                prepare(); start()
            }
        } catch (_: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        preview(null)
    }

    /** Dialog für einen direkten Audio-Link. */
    private fun askLink(slot: Int) {
        val l = prefs.lang
        val input = edit("https://…/adhan.mp3", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val dialog: AlertDialog
        wrap.addView(text(S.linkHint[l], 12f, C.muted, Fonts.light), lp(top = dp(2)))
        wrap.addView(input, lp(top = dp(8)))
        dialog = AlertDialog.Builder(this, dialogStyle())
            .setTitle(S.enterLink[l])
            .setView(ScrollView(this).apply { addView(wrap) })
            .setNegativeButton(S.cancel[l], null)
            .setPositiveButton(S.ok[l]) { _, _ ->
                val url = input.text.toString().trim()
                when {
                    url.isBlank() -> {}
                    AudioDownloader.isYouTube(url) ->
                        Toast.makeText(this, S.youtubeNotSupported[l], Toast.LENGTH_LONG).show()
                    !url.startsWith("https://", ignoreCase = true) ->
                        Toast.makeText(this, S.httpsOnly[l], Toast.LENGTH_LONG).show()
                    else -> startDownload(slot, url, AudioDownloader.nameFrom(url))
                }
            }
            .create()
        dialog.show()
    }

    private fun startDownload(slot: Int, url: String, name: String) {
        val l = prefs.lang
        Toast.makeText(this, S.downloading[l], Toast.LENGTH_SHORT).show()
        AudioDownloader.download(this, url, if (slot < 0) "default" else slot.toString()) { file ->
            if (file == null) {
                Toast.makeText(this, S.downloadFailed[l], Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, S.downloadOk[l], Toast.LENGTH_SHORT).show()
                setSound(slot, file, name)
            }
        }
    }

    // ---------------------------------------------------------------- Aktionen

    private fun pickAudio(req: Int) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        try { startActivityForResult(i, req) } catch (_: Exception) {}
    }

    @Deprecated("Framework-API ohne AndroidX")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        if (resultCode != RESULT_OK || uri == null) return
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}
        val name = displayName(uri)
        when (requestCode) {
            reqAdhan -> setSound(-1, uri.toString(), name)
            in reqPrayerBase..reqPrayerBase + 5 -> setSound(requestCode - reqPrayerBase, uri.toString(), name)
        }
    }

    private fun displayName(uri: Uri): String = try {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        } ?: uri.lastPathSegment ?: "Audio"
    } catch (_: Exception) {
        uri.lastPathSegment ?: "Audio"
    }

    private fun locateNow() {
        val l = prefs.lang
        if (!LocationHelper.hasPermission(this)) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), reqLocation)
            return
        }
        Toast.makeText(this, S.locating[l], Toast.LENGTH_SHORT).show()
        LocationHelper.locate(this) { loc ->
            if (loc == null) {
                Toast.makeText(this, S.locationFailed[l], Toast.LENGTH_LONG).show()
            } else {
                setLocation(loc.latitude, loc.longitude, "", auto = true)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val micOk = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
        when {
            requestCode == reqLocation && LocationHelper.hasPermission(this) -> locateNow()
            requestCode == reqMicRecord && micOk -> {
                val slot = pendingRecordSlot
                pendingRecordSlot = null
                recordDialog(if (slot == null || slot == Int.MIN_VALUE) null else slot)
            }
            requestCode == reqMicClap -> { prefs.clapToStop = micOk; render() }
            else -> render()
        }
    }

    private fun searchCity(q: String, results: LinearLayout) {
        val l = prefs.lang
        if (q.isBlank()) return
        results.removeAllViews()
        results.addView(text("…", 14f, C.muted, Fonts.light), lp(top = dp(6)))
        LocationHelper.search(this, q.trim()) { list ->
            results.removeAllViews()
            if (list.isEmpty()) {
                results.addView(text(S.locationFailed[l], 14f, C.muted, Fonts.light), lp(top = dp(6)))
            }
            list.filter { it.hasLatitude() && it.hasLongitude() }.forEach { a ->
                val name = LocationHelper.label(a)
                results.addView(text("›  $name", 15f, C.cream, Fonts.regular).apply {
                    setPadding(dp(8), dp(10), dp(8), dp(10))
                    clickableRipple(dp(10).toFloat())
                    setOnClickListener { setLocation(a.latitude, a.longitude, name, auto = false) }
                })
            }
        }
    }

    private fun setLocation(lat: Double, lng: Double, name: String, auto: Boolean) {
        val l = prefs.lang
        prefs.lat = lat
        prefs.lng = lng
        prefs.place = name
        prefs.autoLocation = auto
        changed()
        if (name.isBlank()) {
            LocationHelper.placeName(this, lat, lng) { n ->
                if (n != null) {
                    prefs.place = n
                    render()
                }
                Toast.makeText(this, S.locationSet[l].format(n ?: "%.3f, %.3f".format(lat, lng)), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, S.locationSet[l].format(name), Toast.LENGTH_SHORT).show()
        }
    }
}
