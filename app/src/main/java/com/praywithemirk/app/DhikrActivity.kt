package com.praywithemirk.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Adhkar und Sunnah-Gebete zu einem Gebet, mit Tipp-Zähler. */
class DhikrActivity : Activity() {

    companion object {
        const val EXTRA_PRAYER = "prayer"
    }

    private lateinit var prefs: Prefs
    private var prayer = P.FAJR
    private lateinit var content: LinearLayout
    private lateinit var chips: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        C.applyTo(this, prefs.theme)
        prayer = intent.getIntExtra(EXTRA_PRAYER, P.FAJR).let { if (it == P.SUNRISE) P.FAJR else it }
        if (savedInstanceState != null) prayer = savedInstanceState.getInt("prayer", prayer)

        val l = prefs.lang
        val root = FrameLayout(this)
        root.addView(PatternBackground(this), FrameLayout.LayoutParams(-1, -1))
        val scroll = ScrollView(this).apply { isVerticalScrollBarEnabled = false; fitsSystemWindows = true }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(40))
        }
        scroll.addView(col)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        // Kopf
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(text("‹", 32f, C.gold, Fonts.light).apply {
            setPadding(dp(4), 0, dp(16), dp(4))
            setOnClickListener { finish() }
        })
        head.addView(text(S.openAdhkar[l], 18f, C.cream, Fonts.regular), LinearLayout.LayoutParams(0, -2, 1f))
        head.addView(pill(S.reset[l]) { render() })
        col.addView(head)

        // Gebetsauswahl
        chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val hs = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; addView(chips) }
        col.addView(hs, lp(top = dp(16)))

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(content, lp(top = dp(8)))

        setContentView(root)
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("prayer", prayer)
    }

    private fun render() {
        val l = prefs.lang
        chips.removeAllViews()
        for (p in P.SALAH) {
            val sel = p == prayer
            val chip = text(S.prayerNames[p][l], 14f, if (sel) C.onAccent else C.cream, Fonts.medium, Gravity.CENTER).apply {
                background = roundRect(if (sel) C.gold else C.card, dp(18).toFloat(), if (sel) C.gold else C.cardStroke, dp(1))
                setPadding(dp(16), dp(8), dp(16), dp(8))
                setOnClickListener { prayer = p; render() }
            }
            chips.addView(chip, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(8) })
        }

        content.removeAllViews()
        if (prefs.showArabic) content.addView(text(S.prayerArabic[prayer], 34f, C.gold, Fonts.serif, Gravity.CENTER), lp(top = dp(12)))
        content.addView(divider())

        // ---- Erklärung (ein-/ausklappbar, Zustand wird gemerkt)
        val open = prefs.adhkarHelp
        content.addView(card(16).apply {
            val head = LinearLayout(this@DhikrActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            head.addView(text("ⓘ  " + S.helpTitle[l], 15f, C.gold, Fonts.medium), LinearLayout.LayoutParams(0, -2, 1f))
            head.addView(text(if (open) "−" else "+", 22f, C.gold, Fonts.light))
            addView(head)
            if (open) {
                addView(text(S.helpWhat[l], 14f, C.cream, Fonts.light), lp(top = dp(10)))
                for ((t, d) in listOf(S.atAdhan to S.helpAdhan, S.sunnahPrayers to S.helpSunnah, S.afterSalah to S.helpAfter)) {
                    addView(text(t[l], 14f, C.gold, Fonts.medium), lp(top = dp(10)))
                    addView(text(d[l], 14f, C.cream, Fonts.light), lp(top = dp(2)))
                }
                addView(text(S.helpUse[l], 14f, C.cream, Fonts.light), lp(top = dp(12)))
            }
            setOnClickListener { prefs.adhkarHelp = !prefs.adhkarHelp; render() }
        }, lp(top = dp(10)))

        section(S.atAdhan[l])
        content.addView(text(S.helpAdhan[l], 13f, C.muted, Fonts.light), lp(top = dp(4)))
        Adhkar.atAdhan.filter { prefs.allowed(it.source) }.forEach { content.addView(dhikrCard(it), lp(top = dp(10))) }

        section(S.sunnahPrayers[l])
        content.addView(text(S.helpSunnah[l], 13f, C.muted, Fonts.light), lp(top = dp(4)))
        content.addView(card().apply {
            addView(text(Adhkar.sunnahFor(prayer)[l], 15f, C.cream, Fonts.light))
        }, lp(top = dp(10)))

        section(S.afterSalah[l])
        content.addView(text(S.helpAfter[l], 13f, C.muted, Fonts.light), lp(top = dp(4)))
        content.addView(text(S.tapToCount[l], 13f, C.teal, Fonts.light), lp(top = dp(4)))
        Adhkar.afterSalah(prayer).filter { prefs.allowed(it.source) }.forEach { content.addView(dhikrCard(it), lp(top = dp(10))) }
    }

    private fun section(title: String) {
        content.addView(
            text(title.uppercase(), 12f, C.gold, Fonts.medium).apply { letterSpacing = 0.16f },
            lp(top = dp(26))
        )
    }

    private fun dhikrCard(d: Dhikr): View {
        val l = prefs.lang
        val box = card()
        if (prefs.showArabic) box.addView(arabic(d.arabic, 21f))
        if (prefs.showTranslit) box.addView(text(d.translit, 14f, C.teal, Fonts.italic), lp(top = dp(10)))
        box.addView(text(d.meaning[l], 15f, C.cream, Fonts.light), lp(top = dp(10)))
        d.note?.let { box.addView(text(it[l], 13f, C.muted, Fonts.italic), lp(top = dp(8))) }

        val foot = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        foot.addView(text(d.source, 12f, C.faint, Fonts.regular), LinearLayout.LayoutParams(0, -2, 1f))
        val counter: TextView = text("", 14f, C.gold, Fonts.medium, Gravity.CENTER).apply {
            fontFeatureSettings = "tnum"
            setPadding(dp(14), dp(6), dp(14), dp(6))
        }
        foot.addView(counter)
        box.addView(foot, lp(top = dp(12)))

        var n = 0
        fun paint() {
            val done = n >= d.count
            counter.text = if (done) "✓  ${d.count}" else "$n / ${d.count}"
            counter.setTextColor(if (done) C.onAccent else C.gold)
            counter.background = roundRect(if (done) C.gold else Color.TRANSPARENT, dp(16).toFloat(), C.gold, dp(1))
            box.background = roundRect(
                if (done) alpha(C.gold, 31) else C.card, dp(20).toFloat(),
                if (done) C.gold else C.cardStroke, dp(1)
            )
        }
        paint()
        box.setOnClickListener {
            if (n < d.count) {
                n++
                box.performHapticFeedback(
                    if (n == d.count) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.KEYBOARD_TAP
                )
                paint()
            }
        }
        box.setOnLongClickListener { n = 0; paint(); true }
        return box
    }
}
