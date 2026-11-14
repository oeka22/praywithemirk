package com.praywithemirk.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView

/** Gebetsanleitung für Anfänger: Aufbau je Gebet, Waschung, Ablauf. */
class GuideActivity : Activity() {

    companion object {
        const val EXTRA_PRAYER = "prayer"
    }

    private lateinit var prefs: Prefs
    private var prayer = P.FAJR
    private lateinit var content: LinearLayout
    private lateinit var chips: LinearLayout
    private var wuduOpen = false
    private var salahOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        C.applyTo(this, prefs.theme)
        prayer = intent.getIntExtra(EXTRA_PRAYER, P.FAJR).let { if (it == P.SUNRISE) P.FAJR else it }
        if (savedInstanceState != null) {
            prayer = savedInstanceState.getInt("prayer", prayer)
            wuduOpen = savedInstanceState.getBoolean("wudu", false)
            salahOpen = savedInstanceState.getBoolean("salah", false)
        }
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

        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(text("‹", 32f, C.gold, Fonts.light).apply {
            setPadding(dp(4), 0, dp(16), dp(4)); setOnClickListener { finish() }
        })
        head.addView(text(S.guide[l], 20f, C.cream, Fonts.light))
        col.addView(head)

        chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        col.addView(HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false; addView(chips) }, lp(top = dp(16)))

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(content, lp(top = dp(8)))

        setContentView(root)
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("prayer", prayer)
        outState.putBoolean("wudu", wuduOpen)
        outState.putBoolean("salah", salahOpen)
    }

    private fun render() {
        val l = prefs.lang
        chips.removeAllViews()
        for (p in P.SALAH) {
            val sel = p == prayer
            chips.addView(text(S.prayerNames[p][l], 14f, if (sel) C.onAccent else C.cream, Fonts.medium, Gravity.CENTER).apply {
                background = roundRect(if (sel) C.gold else C.card, dp(18).toFloat(), if (sel) C.gold else C.cardStroke, dp(1))
                setPadding(dp(16), dp(8), dp(16), dp(8))
                setOnClickListener { prayer = p; render() }
            }, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(8) })
        }

        content.removeAllViews()

        // ---- Aufbau dieses Gebets
        if (prefs.showArabic) content.addView(text(S.prayerArabic[prayer], 34f, C.gold, Fonts.serif, Gravity.CENTER), lp(top = dp(12)))
        content.addView(divider())
        section(S.structure[l].format(S.prayerNames[prayer][l]))
        content.addView(card().apply {
            val row = LinearLayout(this@GuideActivity).apply { orientation = LinearLayout.HORIZONTAL }
            Guide.parts(prayer).forEachIndexed { i, part ->
                val box = LinearLayout(this@GuideActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    setPadding(dp(4), dp(10), dp(4), dp(10))
                    background = roundRect(
                        if (part.main) C.goldSoft else Color.TRANSPARENT, dp(14).toFloat(),
                        if (part.main) C.gold else C.cardStroke, dp(1)
                    )
                }
                box.addView(text(part.rakat, 26f, if (part.main) C.gold else C.cream, Fonts.light, Gravity.CENTER))
                box.addView(text(S.rakat[l], 11f, C.muted, Fonts.regular, Gravity.CENTER))
                box.addView(text(part.label[l], 12f, C.cream, Fonts.regular, Gravity.CENTER), lp(top = dp(4)))
                row.addView(box, LinearLayout.LayoutParams(0, -1, 1f).apply { if (i > 0) leftMargin = dp(6) })
            }
            addView(row)
            addView(text(Guide.prayerNote(prayer)[l], 14f, C.cream, Fonts.light), lp(top = dp(12)))
        }, lp(top = dp(10)))

        // ---- Waschung
        expandable(S.wuduTitle[l], wuduOpen, { wuduOpen = !wuduOpen; render() }) {
            content.addView(text(Guide.wuduIntro[l], 14f, C.cream, Fonts.light), lp(top = dp(10)))
            Guide.wudu.forEachIndexed { i, s -> content.addView(stepCard(i + 1, s), lp(top = dp(10))) }
            content.addView(note(Guide.wuduNote[l]), lp(top = dp(10)))
        }

        // ---- Ablauf des Gebets
        expandable(S.salahTitle[l], salahOpen, { salahOpen = !salahOpen; render() }) {
            content.addView(text(Guide.salahIntro[l], 14f, C.cream, Fonts.light), lp(top = dp(10)))
            Guide.salah.forEach { s -> content.addView(stepCard(0, s), lp(top = dp(10))) }
            content.addView(note(Guide.salahNote[l]), lp(top = dp(10)))
        }
    }

    private fun section(title: String) {
        content.addView(text(title.uppercase(), 12f, C.gold, Fonts.medium).apply { letterSpacing = 0.14f }, lp(top = dp(18)))
    }

    private fun expandable(title: String, open: Boolean, toggle: () -> Unit, body: () -> Unit) {
        val head = card(16).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(text(title, 16f, C.cream, Fonts.medium), LinearLayout.LayoutParams(0, -2, 1f))
            addView(text(if (open) "−" else "+", 22f, C.gold, Fonts.light))
            clickableRipple(dp(20).toFloat())
            setOnClickListener { toggle() }
        }
        content.addView(head, lp(top = dp(16)))
        if (open) body()
    }

    private fun stepCard(n: Int, s: GuideStep): View {
        val l = prefs.lang
        val box = card(16)
        val title = (if (n > 0) "$n.  " else "") + s.title[l]
        box.addView(text(title, 15f, C.gold, Fonts.medium))
        box.addView(text(s.text[l], 14f, C.cream, Fonts.light), lp(top = dp(6)))
        if (prefs.showArabic) s.arabic?.let { box.addView(arabic(it, 21f), lp(top = dp(10))) }
        if (prefs.showTranslit) s.translit?.let { box.addView(text(it, 13f, C.teal, Fonts.italic), lp(top = dp(8))) }
        s.meaning?.let { box.addView(text(it[l], 13f, C.muted, Fonts.light), lp(top = dp(6))) }
        return box
    }

    private fun note(s: String): View = text(s, 13f, C.muted, Fonts.italic).apply {
        background = roundRect(alpha(C.gold, 20), dp(14).toFloat(), alpha(C.gold, 60), dp(1))
        setPadding(dp(14), dp(12), dp(14), dp(12))
    }
}
