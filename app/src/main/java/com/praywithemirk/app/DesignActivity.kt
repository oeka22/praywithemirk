package com.praywithemirk.app

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView

/**
 * Eigene Design-Seite: alle Designs mit Vorschau.
 * „Klassisch“ = das bisherige Design mit seinen 5 Farben und 4 Stilen (bleibt vollständig erhalten),
 * darunter die 16 neuen Designs.
 */
class DesignActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var col: LinearLayout
    private lateinit var scroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        build()
    }

    private fun build() {
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

    /** Nach einer Auswahl alles mit den neuen Farben neu aufbauen, Scrollposition behalten. */
    private fun apply() {
        val y = scroll.scrollY
        build()
        scroll.post { scroll.scrollTo(0, y) }
    }

    private fun render() {
        val l = prefs.lang
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(text("‹", 32f, C.gold, Fonts.light).apply {
            setPadding(dp(4), 0, dp(16), dp(4)); setOnClickListener { finish() }
        })
        head.addView(text(S.designPage[l], 22f, C.cream, Fonts.light))
        col.addView(head)
        col.addView(text(S.designIntro[l], 14f, C.muted, Fonts.light), lp(top = dp(8)))

        val cols = when {
            resources.configuration.screenWidthDp >= 1000 -> 4
            resources.configuration.screenWidthDp >= 600 -> 3
            else -> 2
        }
        val classicSel = Skins.byId(prefs.skin) == null

        // ---------------- Klassisch (das bisherige Design, mit Farben und Stilen)
        section(S.classicDesign[l])
        col.addView(card(14).apply {
            val row = LinearLayout(this@DesignActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val pv = SkinPreview(this@DesignActivity, null, prefs.theme.p, prefs.style, classicSel)
            pv.setOnClickListener { if (!classicSel) { prefs.skin = "classic"; apply() } }
            row.addView(pv, LinearLayout.LayoutParams(0, dp(if (cols >= 3) 150 else 110), 1f))
            val info = LinearLayout(this@DesignActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), 0, 0, 0) }
            info.addView(text(S.classicDesign[l], 17f, if (classicSel) C.gold else C.cream, Fonts.bold))
            info.addView(text(S.classicDesc[l], 13f, C.muted, Fonts.light), lp(top = dp(4)))
            if (classicSel) info.addView(text("✓ " + S.active[l], 13f, C.gold, Fonts.medium), lp(top = dp(6)))
            row.addView(info, LinearLayout.LayoutParams(0, -2, if (cols >= 3) 2f else 1f))
            addView(row)

            addView(text(S.colors[l], 13f, C.muted, Fonts.medium), lp(top = dp(14)))
            val grid = LinearLayout(this@DesignActivity).apply { orientation = LinearLayout.HORIZONTAL }
            AppTheme.entries.forEachIndexed { idx, t ->
                val sel = classicSel && t == prefs.theme
                val tile = SkinPreview(this@DesignActivity, null, t.p, prefs.style, sel, mini = true)
                tile.contentDescription = t.title[l]
                tile.setOnClickListener { prefs.theme = t; prefs.skin = "classic"; apply() }
                grid.addView(tile, LinearLayout.LayoutParams(0, dp(58), 1f).apply { if (idx > 0) leftMargin = dp(8) })
            }
            addView(grid, lp(top = dp(6)))
            addView(text(prefs.theme.title[l], 13f, C.gold, Fonts.medium, Gravity.CENTER), lp(top = dp(6)))

            addView(text(S.styleLabel[l], 13f, C.muted, Fonts.medium), lp(top = dp(12)))
            val st = AppStyle.entries
            for (rowIdx in 0..1) {
                addView(segmented(st.subList(rowIdx * 2, rowIdx * 2 + 2).map { it.title[l] },
                    if (classicSel) st.indexOf(prefs.style) - rowIdx * 2 else -1) { idx ->
                    prefs.style = st[rowIdx * 2 + idx]; prefs.skin = "classic"; apply()
                }, lp(top = dp(6)))
            }
        }, lp(top = dp(8)))

        // ---------------- Neue Designs
        section(S.newDesigns[l])
        var row: LinearLayout? = null
        Skins.all.forEachIndexed { i, sk ->
            if (i % cols == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                col.addView(row, lp(top = dp(if (i == 0) 8 else 12)))
            }
            row!!.addView(skinTile(sk), LinearLayout.LayoutParams(0, -2, 1f).apply { if (i % cols > 0) leftMargin = dp(12) })
        }
        // letzte Reihe auffüllen, damit Kacheln gleich breit bleiben
        val rest = Skins.all.size % cols
        if (rest > 0) repeat(cols - rest) { row!!.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f).apply { leftMargin = dp(12) }) }
    }

    private fun skinTile(sk: Skin): View {
        val l = prefs.lang
        val sel = prefs.skin == sk.id
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val pv = SkinPreview(this, sk, sk.p, AppStyle.CLASSIC, sel)
        box.addView(pv, LinearLayout.LayoutParams(-1, dp(118)))
        box.addView(text((if (sel) "✓ " else "") + sk.title[l], 15f, if (sel) C.gold else C.cream, Fonts.bold).apply { maxLines = 1 }, lp(top = dp(6)))
        box.addView(text(sk.desc[l], 12f, C.muted, Fonts.light).apply { maxLines = 2 }, lp(top = dp(2)))
        box.setOnClickListener { if (!sel) { prefs.skin = sk.id; apply() } }
        return box
    }

    private fun section(title: String) {
        col.addView(text(title.uppercase(), 12f, C.gold, Fonts.medium).apply { letterSpacing = 0.16f }, lp(top = dp(24)))
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
}

/**
 * Miniatur des Hauptbildschirms (Querformat) für ein Design.
 * Zeichnet mit den Farben des Designs, ohne die aktiven Farben der Seite dauerhaft zu ändern.
 */
class SkinPreview(
    context: Context,
    private val skin: Skin?,
    private val pal: Palette,
    private val style: AppStyle,
    private val selected: Boolean,
    private val mini: Boolean = false,
) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val r = RectF()
    private var bg: PatternBackground? = null
    private var art: HeroArtView? = null

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) } // kleine Ansicht: Clip/Unschärfe zuverlässig

    private fun d(v: Float) = context.dp(v).toFloat()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        // aktive Farben merken und für die Zeichnung tauschen
        val oldPal = C.palette; val oldSkin = C.skin; val oldStyle = C.style
        C.skin = skin; C.usePalette(pal); C.style = style
        try {
            val rad = d(12f)
            val save = c.save()
            val clip = android.graphics.Path().apply { addRoundRect(0f, 0f, w, h, rad, rad, android.graphics.Path.Direction.CW) }
            c.clipPath(clip)
            // Hintergrund wie in der App
            val b = bg ?: PatternBackground(context).also { bg = it }
            if (b.width != width || b.height != height) { b.measure(0, 0); b.layout(0, 0, width, height) }
            b.draw(c)

            if (mini) {
                drawClassicRing(c, w * 0.32f, h * 0.45f, h * 0.26f)
                p.color = C.cream
                c.drawRoundRect(w * 0.58f, h * 0.32f, w * 0.86f, h * 0.40f, 3f, 3f, p)
                p.color = C.muted
                c.drawRoundRect(w * 0.58f, h * 0.50f, w * 0.80f, h * 0.56f, 3f, 3f, p)
            } else {
                drawScreen(c, w, h)
            }
            c.restoreToCount(save)
            if (selected) {
                p.style = Paint.Style.STROKE; p.strokeWidth = d(2.5f); p.color = col(pal.accent)
                c.drawRoundRect(d(1.5f), d(1.5f), w - d(1.5f), h - d(1.5f), rad, rad, p)
                p.style = Paint.Style.FILL
            } else {
                p.style = Paint.Style.STROKE; p.strokeWidth = d(1f); p.color = col(pal.cardStroke)
                c.drawRoundRect(d(0.5f), d(0.5f), w - d(0.5f), h - d(0.5f), rad, rad, p)
                p.style = Paint.Style.FILL
            }
        } finally {
            C.skin = oldSkin; C.usePalette(oldPal); C.style = oldStyle
        }
    }

    private fun drawClassicRing(c: Canvas, cx: Float, cy: Float, rr: Float) {
        p.style = Paint.Style.STROKE
        p.strokeWidth = rr * 0.12f
        p.color = alpha(C.cream, 40)
        c.drawCircle(cx, cy, rr, p)
        p.color = C.gold
        p.strokeCap = Paint.Cap.ROUND
        r.set(cx - rr, cy - rr, cx + rr, cy + rr)
        c.drawArc(r, -90f, 230f, false, p)
        p.style = Paint.Style.FILL
        p.strokeCap = Paint.Cap.BUTT
    }

    /** Hauptgrafik links, Ort/Uhr/Hadith rechts, Gebetsliste unten. */
    private fun drawScreen(c: Canvas, w: Float, h: Float) {
        val pad = h * 0.07f
        val listH = h * 0.24f
        val heroS = h - pad * 3 - listH
        val hx = pad; val hy = pad
        val hero = skin?.hero ?: Hero.RING
        if (skin == null || hero == Hero.RING) {
            drawClassicRing(c, hx + heroS / 2, hy + heroS / 2, heroS * 0.42f)
        } else {
            val a = art ?: HeroArtView(context, skin).also { art = it }
            a.progress = 0.62f; a.nowHour = 15.3f; a.nextIndex = 4
            val s = heroS.toInt()
            if (a.width != s) { a.measure(0, 0); a.layout(0, 0, s, s) }
            val sv = c.save(); c.translate(hx, hy); a.draw(c); c.restoreToCount(sv)
        }
        // Text in der Grafik
        val fg = skin?.let { col(it.heroFg) } ?: C.cream
        val cx = hx + heroS / 2
        val nameY: Float; val valY: Float
        when (hero) {
            Hero.SUNARC -> { nameY = hy + heroS * 0.74f; valY = hy + heroS * 0.93f }
            Hero.ARCH, Hero.LANTERN -> { nameY = hy + heroS * 0.52f; valY = hy + heroS * 0.70f }
            else -> { nameY = hy + heroS * 0.44f; valY = hy + heroS * 0.66f }
        }
        val tx = if (hero == Hero.TYPO) hx + heroS * 0.06f else if (hero == Hero.MOON) hx + heroS * 0.3f else cx
        p.textAlign = if (hero == Hero.TYPO) Paint.Align.LEFT else Paint.Align.CENTER
        p.typeface = SkinFonts.nameBold()
        p.textSize = heroS * 0.11f
        p.color = when {
            skin == null -> C.gold
            hero == Hero.ARCH || hero == Hero.SOFT || hero == Hero.FLAP -> col(skin.alt)
            hero == Hero.KISWA -> C.gold
            else -> fg
        }
        if (hero != Hero.DIAL24) c.drawText("Asr", tx, nameY, p)
        p.typeface = SkinFonts.digits()
        p.textSize = heroS * (when (hero) { Hero.DIAL24 -> 0.12f; Hero.TYPO -> 0.26f; Hero.SUNARC -> 0.16f; else -> 0.19f })
        p.color = fg
        c.drawText("1:24", tx, if (hero == Hero.DIAL24) hy + heroS * 0.55f else valY, p)
        p.textAlign = Paint.Align.LEFT

        // rechte Spalte: Ort/Uhr und Hadith-Karte
        val rx = hx + heroS + pad * 1.4f
        val rw = w - rx - pad
        p.typeface = SkinFonts.digits()
        p.textSize = heroS * 0.16f
        p.color = C.cream
        c.drawText("14:01", rx, hy + heroS * 0.16f, p)
        p.color = C.muted
        c.drawRoundRect(rx, hy + heroS * 0.24f, rx + rw * 0.45f, hy + heroS * 0.29f, 3f, 3f, p)
        p.color = C.card
        r.set(rx, hy + heroS * 0.38f, rx + rw, hy + heroS)
        c.drawRoundRect(r, d(8f), d(8f), p)
        p.color = alpha(C.cream, 150)
        for (k in 0..2) c.drawRoundRect(rx + rw * 0.08f, r.top + r.height() * (0.22f + k * 0.22f), rx + rw * (0.9f - k * 0.18f), r.top + r.height() * (0.29f + k * 0.22f), 3f, 3f, p)

        // Gebetsliste unten (Kacheln je Design)
        val ly = h - pad - listH
        val gap = w * 0.012f
        val tw = (w - pad * 2 - gap * 5) / 6
        val list = skin?.list ?: ListStyle.TILES
        for (i in 0 until 6) {
            val x = pad + i * (tw + gap)
            r.set(x, ly, x + tw, ly + listH)
            val cur = i == 3
            p.color = when {
                list == ListStyle.FLAP -> if (cur) Color.parseColor("#2E2A22") else Color.parseColor("#1E1E1E")
                cur -> C.goldSoft
                else -> C.card
            }
            when (list) {
                ListStyle.ARCH_TILES -> {
                    val path = android.graphics.Path()
                    path.moveTo(r.left, r.bottom); path.lineTo(r.left, r.top + tw * 0.45f)
                    path.quadTo(r.left, r.top, r.centerX(), r.top); path.quadTo(r.right, r.top, r.right, r.top + tw * 0.45f)
                    path.lineTo(r.right, r.bottom); path.close()
                    c.drawPath(path, p)
                }
                ListStyle.ROWS -> c.drawRoundRect(r, d(4f), d(4f), p)
                else -> c.drawRoundRect(r, d(if (skin?.id == "zellige") 1f else 5f), d(if (skin?.id == "zellige") 1f else 5f), p)
            }
            p.color = if (cur) C.gold else if (i == 1) C.muted else C.cream
            p.textAlign = Paint.Align.CENTER
            p.typeface = SkinFonts.digits()
            p.textSize = listH * 0.32f
            c.drawText(arrayOf("5:48", "7:31", "12:47", "15:21", "17:52", "19:24")[i], r.centerX(), r.top + listH * 0.72f, p)
            p.textAlign = Paint.Align.LEFT
        }
        p.typeface = Typeface.DEFAULT
    }
}
