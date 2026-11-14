package com.praywithemirk.app

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Eine Farbpalette (Design). */
class Palette(
    val bgTop: String, val bgBottom: String,
    val card: String, val cardStroke: String,
    val accent: String, val accentSoft: String, val onAccent: String,
    val text: String, val muted: String, val faint: String,
    val secondary: String, val pattern: String,
    val light: Boolean,
)

/** Die fünf wählbaren Designs. NIGHT (das ursprüngliche) ist Standard. */
enum class AppTheme(val id: String, val title: T, val p: Palette) {
    NIGHT(
        "night", T("Nacht (Standard)", "Gece (Standart)", "Night (default)"),
        Palette("#0F1E26", "#163A42", "#14FFFFFF", "#22FFFFFF", "#D9B77C", "#33D9B77C", "#0F1E26",
            "#F3EBDD", "#9FB3B8", "#5E7A80", "#7FC8BE", "#0ED9B77C", false)
    ),
    MIDNIGHT(
        "midnight", T("Mitternacht", "Gece yarısı", "Midnight"),
        Palette("#05070A", "#0B1216", "#0FFFFFFF", "#1AFFFFFF", "#C9A86A", "#2EC9A86A", "#05070A",
            "#E8E2D6", "#8A979B", "#4A5559", "#6FA8A0", "#0AC9A86A", false)
    ),
    DUSK(
        "dusk", T("Abenddämmerung", "Akşam alacası", "Dusk"),
        Palette("#241A30", "#4A2C3F", "#16FFFFFF", "#24FFFFFF", "#E8B98F", "#33E8B98F", "#241A30",
            "#F6E9E1", "#C2A9B4", "#7D6573", "#B9A2D8", "#10E8B98F", false)
    ),
    SAND(
        "sand", T("Sandstein (hell)", "Kum taşı (açık)", "Sandstone (light)"),
        Palette("#F7F1E5", "#E9DCC3", "#B3FFFFFF", "#26745A36", "#9C6B2F", "#269C6B2F", "#FFFFFF",
            "#3A2E22", "#7A6A58", "#A8977F", "#2F6F68", "#149C6B2F", true)
    ),
    MINT(
        "mint", T("Minze (hell)", "Nane (açık)", "Mint (light)"),
        Palette("#EFF6F3", "#D6E9E2", "#B3FFFFFF", "#22285E55", "#2A7F70", "#222A7F70", "#FFFFFF",
            "#1D3532", "#5B7A74", "#93ABA5", "#A07A3E", "#122A7F70", true)
    );

    companion object {
        fun byId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: NIGHT
    }
}

/** Darstellungsstil – unabhängig von der Farbpalette kombinierbar. */
enum class AppStyle(val id: String, val title: T) {
    CLASSIC("classic", T("Klassisch", "Klasik", "Classic")),
    GLASS("glass", T("Glas & Licht", "Cam ve ışık", "Glass & light")),
    SOFT3D("soft3d", T("Soft 3D", "Yumuşak 3D", "Soft 3D")),
    GLOW("glow", T("Leuchtende Nacht", "Işıldayan gece", "Glowing night"));

    companion object {
        fun byId(id: String?): AppStyle = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

/** Aktive Farben – werden beim Start jeder Seite aus dem gewählten Design gesetzt. */
object C {
    var bgTop = 0; var bgBottom = 0; var card = 0; var cardStroke = 0
    var gold = 0; var goldSoft = 0; var onAccent = 0
    var cream = 0; var muted = 0; var faint = 0; var teal = 0; var pattern = 0
    var light = false
    var theme = AppTheme.NIGHT
    var style = AppStyle.CLASSIC
    /** Gewähltes neues Design (null = klassische Designs mit Farbe + Stil). */
    var skin: Skin? = null

    init { use(AppTheme.NIGHT) }

    fun use(t: AppTheme) {
        theme = t
        usePalette(t.p)
    }

    var palette: Palette = AppTheme.NIGHT.p
        private set

    fun usePalette(p: Palette) {
        palette = p
        fun c(s: String) = Color.parseColor(s)
        bgTop = c(p.bgTop); bgBottom = c(p.bgBottom); card = c(p.card); cardStroke = c(p.cardStroke)
        gold = c(p.accent); goldSoft = c(p.accentSoft); onAccent = c(p.onAccent)
        cream = c(p.text); muted = c(p.muted); faint = c(p.faint); teal = c(p.secondary)
        pattern = c(p.pattern); light = p.light
    }

    /** Design anwenden und Fensterfarben/Statusleiste anpassen. */
    @Suppress("DEPRECATION")
    fun applyTo(a: android.app.Activity, t: AppTheme) {
        val sk = Skins.byId(Prefs(a).skin)
        skin = sk
        if (sk != null) {
            theme = t
            usePalette(sk.p)
            style = AppStyle.CLASSIC
        } else {
            use(t)
            style = Prefs(a).style
        }
        val w = a.window
        w.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgTop))
        w.statusBarColor = bgTop
        w.navigationBarColor = bgBottom
        var flags = w.decorView.systemUiVisibility
        flags = if (light) flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        else flags and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR).inv()
        w.decorView.systemUiVisibility = flags
    }
}

/** Farbe mit neuem Alpha (0–255). */
fun alpha(color: Int, a: Int): Int = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color))

fun Context.dp(v: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics).toInt()
fun Context.dp(v: Int): Int = dp(v.toFloat())

object Fonts {
    val light: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    val thin: Typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
    val regular: Typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    val medium: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    val bold: Typeface = Typeface.create("sans-serif", Typeface.BOLD)
    val serif: Typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    val italic: Typeface = Typeface.create("sans-serif-light", Typeface.ITALIC)
}

fun Context.text(
    s: CharSequence = "",
    size: Float = 15f,
    color: Int = C.cream,
    tf: Typeface = Fonts.regular,
    gravity: Int = Gravity.START,
): TextView = TextView(this).apply {
    text = s
    setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    setTextColor(color)
    typeface = tf
    this.gravity = gravity
    setLineSpacing(0f, 1.15f)
}

/** Arabischer Text: rechtsbündig, größer, mit großzügigem Zeilenabstand. */
fun Context.arabic(s: String, size: Float = 22f, color: Int = C.cream): TextView =
    text(s, size, color, Fonts.serif, Gravity.END).apply {
        textDirection = View.TEXT_DIRECTION_RTL
        setLineSpacing(0f, 1.45f)
    }

fun roundRect(color: Int, radius: Float, stroke: Int = 0, strokeWidth: Int = 0): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius
        setColor(color)
        if (strokeWidth > 0) setStroke(strokeWidth, stroke)
    }

/** Karte mit weichem, halbtransparentem Hintergrund. */
fun Context.card(padding: Int = 18): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    if (C.style == AppStyle.CLASSIC) {
        background = roundRect(C.card, dp(20).toFloat(), C.cardStroke, dp(1))
        setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
    } else {
        val d = StyledCard(this@card, dp(20).toFloat())
        background = d // Hardware-Darstellung: funktioniert auch bei sehr langen Karten
        val m = d.margin
        setPadding(dp(padding) + m, dp(padding) + m - d.shiftUp, dp(padding) + m, dp(padding) + m + d.shiftUp)
    }
}

/** Hervorhebung der aktuellen Zeile passend zum Stil. */
fun Context.rowHighlight(): android.graphics.drawable.Drawable =
    if (C.style == AppStyle.CLASSIC) roundRect(C.goldSoft, dp(14).toFloat()) else RowHighlight(this, dp(14).toFloat())

private fun mix(a: Int, b: Int, t: Float): Int = Color.rgb(
    (Color.red(a) + (Color.red(b) - Color.red(a)) * t).toInt(),
    (Color.green(a) + (Color.green(b) - Color.green(a)) * t).toInt(),
    (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t).toInt()
)
fun darker(c: Int, t: Float) = mix(c, Color.BLACK, t)
fun lighter(c: Int, t: Float) = mix(c, Color.WHITE, t)

/** Kartenhintergrund mit Licht und Schatten (Glas, Soft 3D, Leuchtende Nacht). */
class StyledCard(private val ctx: Context, private val radius: Float) : android.graphics.drawable.Drawable() {
    val margin: Int = when (C.style) { AppStyle.SOFT3D -> ctx.dp(9); AppStyle.GLOW -> ctx.dp(10); else -> ctx.dp(6) }
    /** Inhalt etwas nach oben, damit unten Platz für den Schatten bleibt. */
    val shiftUp: Int = if (C.style == AppStyle.GLOW) ctx.dp(3) else 0
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val r = RectF()

    /** Weicher Schatten: ab Android 9 per Hardware (setShadowLayer), davor als einfache Fläche. */
    private fun shadow(c: Canvas, l: Float, t: Float, rr: Float, bb: Float, blur: Float, dx: Float, dy: Float, color: Int, fill: Int) {
        val sp = Paint(Paint.ANTI_ALIAS_FLAG)
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            sp.color = fill
            sp.setShadowLayer(blur, dx, dy, color)
            c.drawRoundRect(l, t, rr, bb, radius, radius, sp)
        } else {
            sp.color = alpha(color, Color.alpha(color) / 2)
            c.drawRoundRect(l + dx, t + dy, rr + dx, bb + dy, radius, radius, sp)
        }
    }

    override fun draw(c: Canvas) {
        val b = bounds
        val m = margin.toFloat()
        r.set(b.left + m, b.top + m - shiftUp, b.right - m, b.bottom - m - shiftUp)
        p.reset(); p.isAntiAlias = true
        when (C.style) {
            AppStyle.GLASS -> {
                shadow(c, r.left, r.top, r.right, r.bottom, m, 0f, m / 2, Color.argb(110, 0, 0, 0), alpha(C.bgTop, 255))
                // Schattenfläche wieder mit dem Hintergrund überdecken, damit das Glas durchscheinend bleibt
                p.shader = LinearGradient(r.left, r.top, r.right * 0.6f, r.bottom,
                    alpha(Color.WHITE, if (C.light) 170 else 38), alpha(Color.WHITE, if (C.light) 110 else 10), Shader.TileMode.CLAMP)
                c.drawRoundRect(r, radius, radius, p)
                p.shader = LinearGradient(0f, r.top, 0f, r.bottom,
                    alpha(Color.WHITE, if (C.light) 230 else 90), alpha(Color.WHITE, if (C.light) 60 else 12), Shader.TileMode.CLAMP)
                p.style = Paint.Style.STROKE; p.strokeWidth = ctx.dp(1f).toFloat()
                c.drawRoundRect(r, radius, radius, p)
            }
            AppStyle.SOFT3D -> {
                val base = C.bgTop
                val off = m * 0.5f
                shadow(c, r.left, r.top, r.right, r.bottom, m * 0.9f, off, off, darker(base, if (C.light) 0.18f else 0.45f), base)
                shadow(c, r.left, r.top, r.right, r.bottom, m * 0.9f, -off, -off, lighter(base, if (C.light) 0.7f else 0.10f), base)
                p.color = base
                c.drawRoundRect(r, radius, radius, p)
            }
            else -> { // GLOW
                val top = lighter(C.bgTop, if (C.light) 0.6f else 0.09f)
                shadow(c, r.left, r.top, r.right, r.bottom, m, 0f, m * 0.7f, Color.argb(190, 0, 0, 0), top)
                p.shader = LinearGradient(0f, r.top, 0f, r.bottom,
                    top, darker(C.bgTop, if (C.light) 0f else 0.05f), Shader.TileMode.CLAMP)
                c.drawRoundRect(r, radius, radius, p)
                p.shader = LinearGradient(0f, r.top, 0f, r.top + radius * 2,
                    alpha(Color.WHITE, if (C.light) 200 else 45), alpha(Color.WHITE, 0), Shader.TileMode.CLAMP)
                p.style = Paint.Style.STROKE; p.strokeWidth = ctx.dp(1f).toFloat()
                c.drawRoundRect(r, radius, radius, p)
            }
        }
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

/** Leuchtende/eingedrückte Hervorhebung der aktuellen Gebetszeile. */
class RowHighlight(private val ctx: Context, private val radius: Float) : android.graphics.drawable.Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val r = RectF()
    override fun draw(c: Canvas) {
        r.set(bounds)
        p.reset(); p.isAntiAlias = true
        when (C.style) {
            AppStyle.SOFT3D -> {
                p.color = darker(C.bgTop, if (C.light) 0.05f else 0.12f)
                c.drawRoundRect(r, radius, radius, p)
                p.style = Paint.Style.STROKE; p.strokeWidth = ctx.dp(2f).toFloat()
                p.shader = LinearGradient(r.left, r.top, r.right, r.bottom,
                    darker(C.bgTop, if (C.light) 0.2f else 0.5f), lighter(C.bgTop, if (C.light) 0.8f else 0.12f), Shader.TileMode.CLAMP)
                c.drawRoundRect(r, radius, radius, p)
            }
            AppStyle.GLOW -> {
                p.shader = LinearGradient(r.left, 0f, r.right, 0f, alpha(C.gold, 70), alpha(C.gold, 0), Shader.TileMode.CLAMP)
                c.drawRoundRect(r, radius, radius, p)
                p.shader = null
                p.color = C.gold
                p.setShadowLayer(ctx.dp(8f).toFloat(), 0f, 0f, C.gold)
                c.drawRoundRect(r.left, r.top + ctx.dp(6), r.left + ctx.dp(3), r.bottom - ctx.dp(6), 4f, 4f, p)
            }
            else -> { // GLASS
                p.shader = LinearGradient(r.left, 0f, r.right, 0f, alpha(C.gold, 80), alpha(C.gold, 18), Shader.TileMode.CLAMP)
                c.drawRoundRect(r, radius, radius, p)
                p.shader = null
                p.style = Paint.Style.STROKE; p.strokeWidth = ctx.dp(1f).toFloat()
                p.color = alpha(C.gold, 120)
                c.drawRoundRect(r, radius, radius, p)
            }
        }
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

fun View.clickableRipple(radius: Float) {
    val content = background
    background = RippleDrawable(ColorStateList.valueOf(C.goldSoft), content, roundRect(Color.WHITE, radius))
    isClickable = true
    isFocusable = true
}

/** Pillen-Button. */
fun Context.pill(label: String, filled: Boolean = false, onClick: () -> Unit): TextView =
    text(label, 14f, if (filled) C.onAccent else C.gold, Fonts.medium, Gravity.CENTER).apply {
        background = roundRect(if (filled) C.gold else Color.TRANSPARENT, dp(22).toFloat(), C.gold, dp(1))
        setPadding(dp(18), dp(10), dp(18), dp(10))
        clickableRipple(dp(22).toFloat())
        setOnClickListener { onClick() }
    }

fun lp(w: Int = ViewGroup.LayoutParams.MATCH_PARENT, h: Int = ViewGroup.LayoutParams.WRAP_CONTENT, top: Int = 0, bottom: Int = 0) =
    LinearLayout.LayoutParams(w, h).apply { topMargin = top; bottomMargin = bottom }

/**
 * Hintergrund: Verlauf von Nachtblau zu Petrol mit einem sehr dezenten
 * achtzackigen Sternmuster (islamische Geometrie).
 */
class PatternBackground(context: Context) : View(context) {
    private val bg = Paint()
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = C.pattern
        strokeWidth = context.dp(1f).toFloat()
    }
    private val star = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bg.shader = LinearGradient(0f, 0f, w * 0.3f, h.toFloat(), C.bgTop, C.bgBottom, Shader.TileMode.CLAMP)
    }

    private val light = Paint(Paint.ANTI_ALIAS_FLAG)
    private val vignette = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        C.skin?.let { drawSkinBackground(canvas, it, w, h); return }
        when (C.style) {
            AppStyle.SOFT3D -> { canvas.drawColor(C.bgTop); return }
            AppStyle.GLOW -> {
                canvas.drawRect(0f, 0f, w, h, bg)
                light.shader = android.graphics.RadialGradient(w * 0.5f, -h * 0.05f, maxOf(w, 1f) * 1.1f,
                    alpha(C.gold, if (C.light) 70 else 55), alpha(C.gold, 0), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, w, h, light)
                vignette.shader = android.graphics.RadialGradient(w / 2, h * 0.45f, maxOf(w, h, 1f) * 0.8f,
                    Color.TRANSPARENT, Color.argb(if (C.light) 30 else 140, 0, 0, 0), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, w, h, vignette)
                return
            }
            AppStyle.GLASS -> {
                canvas.drawRect(0f, 0f, w, h, bg)
                light.shader = android.graphics.RadialGradient(w * 0.2f, h * 0.08f, maxOf(w, 1f) * 0.9f,
                    alpha(lighter(C.teal, 0.3f), if (C.light) 90 else 70), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, w, h, light)
            }
            else -> canvas.drawRect(0f, 0f, w, h, bg)
        }
        val cell = context.dp(84).toFloat()
        val r = cell * 0.36f
        var y = 0f
        var row = 0
        while (y < height + cell) {
            var x = if (row % 2 == 0) 0f else cell / 2
            while (x < width + cell) {
                drawStar(canvas, x, y, r)
                x += cell
            }
            y += cell * 0.866f
            row++
        }
    }

    private val dp1 = context.dp(1f).toFloat()

    /** Hintergründe der neuen Designs (Verlauf + Dekoration). */
    private fun drawSkinBackground(c: Canvas, sk: Skin, w: Float, h: Float) {
        c.drawRect(0f, 0f, w, h, bg)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val rnd = java.util.Random(7)
        when (sk.deco) {
            Deco.STARS -> {
                for (i in 0 until 90) {
                    p.color = Color.argb(60 + rnd.nextInt(160), 255, 255, 255)
                    c.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h, dp1 * (0.5f + rnd.nextFloat() * 1.1f), p)
                }
            }
            Deco.EMBERS -> {
                p.shader = android.graphics.RadialGradient(w / 2, h * 1.1f, maxOf(w, h) * 0.7f, Color.argb(150, 120, 50, 18), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                c.drawRect(0f, 0f, w, h, p); p.shader = null
                for (i in 0 until 40) {
                    p.color = Color.argb(60 + rnd.nextInt(120), 255, 170, 100)
                    c.drawCircle(rnd.nextFloat() * w, h - rnd.nextFloat() * h * 0.45f, dp1 * (1f + rnd.nextFloat() * 1.5f), p)
                }
            }
            Deco.GLOW -> {
                p.shader = android.graphics.RadialGradient(w * 0.3f, h * 0.35f, maxOf(w, h) * 0.6f, Color.argb(110, 90, 50, 18), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                c.drawRect(0f, 0f, w, h, p)
            }
            Deco.SKY -> {
                p.shader = LinearGradient(0f, 0f, 0f, h, intArrayOf(Color.parseColor("#F7B49A"), Color.parseColor("#F6D6B0"), Color.parseColor("#CFE3EE"), Color.parseColor("#A9C9DE")), floatArrayOf(0f, 0.35f, 0.75f, 1f), Shader.TileMode.CLAMP)
                c.drawRect(0f, 0f, w, h, p)
            }
            Deco.LEAVES -> {
                p.style = Paint.Style.STROKE; p.strokeWidth = dp1 * 1.6f; p.color = alpha(C.gold, 34)
                for ((fx, fy, sc, rot) in listOf(arrayOf(0.8f, 0.05f, 1.2f, 15f), arrayOf(-0.05f, 0.4f, 1f, -20f), arrayOf(0.75f, 0.7f, 0.9f, 30f), arrayOf(0.4f, 0.85f, 0.8f, -10f))) {
                    c.save(); c.translate(fx * w, fy * h); c.rotate(rot); val k = dp1 * 1.6f * sc
                    val path = Path()
                    path.moveTo(25 * k, 78 * k); path.lineTo(25 * k, 4 * k)
                    path.moveTo(25 * k, 60 * k); path.cubicTo(10 * k, 52 * k, 4 * k, 38 * k, 8 * k, 24 * k); path.cubicTo(20 * k, 30 * k, 25 * k, 44 * k, 25 * k, 60 * k)
                    path.moveTo(25 * k, 44 * k); path.cubicTo(40 * k, 36 * k, 46 * k, 22 * k, 42 * k, 8 * k); path.cubicTo(30 * k, 14 * k, 25 * k, 28 * k, 25 * k, 44 * k)
                    c.drawPath(path, p); c.restore()
                }
            }
            Deco.WOOD -> {
                p.strokeWidth = dp1
                var x = 0f
                while (x < w) {
                    p.color = Color.argb(18 + rnd.nextInt(26), 0, 0, 0)
                    c.drawLine(x, 0f, x + rnd.nextFloat() * dp1 * 6, h, p)
                    x += dp1 * (3 + rnd.nextInt(6))
                }
            }
            Deco.GRAIN -> {
                p.color = Color.argb(14, 255, 255, 255)
                var y = 0f
                while (y < h) { var x = 0f; while (x < w) { c.drawCircle(x, y, dp1 * 0.6f, p); x += dp1 * 5 }; y += dp1 * 5 }
            }
            Deco.ARCADES -> {
                p.style = Paint.Style.STROKE; p.strokeWidth = dp1 * 2; p.color = alpha(C.gold, 50)
                val n = maxOf(4, (w / (dp1 * 72)).toInt()); val aw = w / n; val ah = minOf(h * 0.16f, dp1 * 110)
                for (i in 0 until n) {
                    val l = i * aw + dp1 * 4; val r = (i + 1) * aw - dp1 * 4
                    val path = Path(); path.moveTo(l, ah); path.lineTo(l, (r - l) / 2); path.arcTo(RectF(l, 0f, r, r - l), 180f, 180f); path.lineTo(r, ah)
                    c.drawPath(path, p)
                }
            }
            Deco.MOSAIC -> {
                val cell = dp1 * 44
                var y = 0f
                while (y < h) {
                    var x = 0f
                    while (x < w) {
                        p.style = Paint.Style.FILL
                        p.color = Color.argb(30, 42, 157, 143); c.drawRect(x, y, x + cell / 2, y + cell / 2, p); c.drawRect(x + cell / 2, y + cell / 2, x + cell, y + cell, p)
                        p.color = Color.argb(30, 231, 111, 81); c.drawRect(x + cell / 2, y, x + cell, y + cell / 2, p); c.drawRect(x, y + cell / 2, x + cell / 2, y + cell, p)
                        p.style = Paint.Style.STROKE; p.strokeWidth = dp1; p.color = Color.argb(45, 242, 193, 78)
                        c.drawCircle(x + cell / 2, y + cell / 2, cell * 0.24f, p)
                        x += cell
                    }
                    y += cell
                }
            }
            Deco.KISWA -> {
                p.color = Color.argb(10, 255, 255, 255); p.strokeWidth = dp1
                var x = -h
                while (x < w) { c.drawLine(x, 0f, x + h, h, p); x += dp1 * 9 }
                val bh = dp1 * 46
                p.color = Color.parseColor("#1A1712"); c.drawRect(0f, 0f, w, bh, p)
                p.color = C.gold; c.drawRect(0f, bh - dp1 * 2, w, bh, p)
                p.color = alpha(C.gold, 140)
                var xx = 0f; while (xx < w) { c.drawLine(xx, dp1 * 4, xx, bh - dp1 * 4, p); xx += dp1 * 12 }
                p.color = Color.parseColor("#1A1712"); val tw = minOf(w * 0.7f, dp1 * 320)
                c.drawRect(w / 2 - tw / 2, dp1 * 3, w / 2 + tw / 2, bh - dp1 * 4, p)
                p.color = C.gold; p.textAlign = Paint.Align.CENTER; p.textSize = dp1 * 22
                c.drawText("حَيَّ عَلَى الصَّلَاةِ", w / 2, bh * 0.7f, p)
            }
            Deco.FANUS, Deco.MASHRABIYA -> {
                // feines Girih-Muster, warmes Licht von oben, hängende Laterne(n) oben rechts
                val wide = w > dp1 * 600
                Orient.drawGirih(c, w, h, dp1 * (if (wide) 64f else 44f), C.pattern, dp1 * 1.1f)
                val lamp = col(sk.heroBg)
                Orient.glow(c, w * 0.5f, h * 0.3f, maxOf(w, h) * 0.45f, alpha(lamp, if (C.light) 60 else 40))
                if (sk.deco == Deco.FANUS) {
                    if (wide) {
                        Orient.drawLantern(c, w - dp1 * 66, dp1 * 22, 1.25f, lamp, C.gold, dp1)
                        Orient.drawLantern(c, w - dp1 * 130, dp1 * 8, 0.9f, lamp, C.gold, dp1)
                    } else Orient.drawLantern(c, w - dp1 * 20, dp1 * 10, 0.75f, lamp, C.gold, dp1)
                } else {
                    if (wide) Orient.drawLantern(c, w - dp1 * 54, dp1 * 6, 0.8f, lamp, C.gold, dp1)
                    else Orient.drawLantern(c, w - dp1 * 18, dp1 * 4, 0.6f, lamp, C.gold, dp1)
                }
            }
            Deco.PATTERN, Deco.NONE -> {}
        }
    }

    private fun drawStar(c: Canvas, cx: Float, cy: Float, r: Float) {
        // Zwei überlagerte Quadrate = achtzackiger Stern (Rub el Hizb)
        for (k in 0..1) {
            star.reset()
            for (i in 0..3) {
                val a = Math.toRadians(45.0 * k + 90.0 * i + 45.0)
                val px = cx + (r * cos(a)).toFloat()
                val py = cy + (r * sin(a)).toFloat()
                if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
            }
            star.close()
            c.drawPath(star, line)
        }
    }
}

/** Ring, der den Fortschritt zwischen letztem und nächstem Gebet zeigt. */
class RingView(context: Context) : View(context) {
    var progress: Float = 0f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }

    private var stroke = context.dp(6f).toFloat()
    private val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = stroke; color = alpha(C.cream, 30)
    }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = stroke; strokeCap = Paint.Cap.ROUND
    }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = C.gold }
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = alpha(C.gold, 60) }
    private val ticks = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = alpha(C.cream, 40); strokeWidth = context.dp(1f).toFloat()
    }
    private val oval = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Strichstärke wächst mit der Ringgröße (gut lesbar als Wanduhr)
        stroke = maxOf(context.dp(6f).toFloat(), min(w, h) * 0.035f)
        track.strokeWidth = stroke; arc.strokeWidth = stroke
        ticks.strokeWidth = maxOf(context.dp(1f).toFloat(), stroke * 0.2f)
        val pad = stroke * 2.2f
        val s = min(w, h).toFloat()
        val l = (w - s) / 2 + pad
        val t = (h - s) / 2 + pad
        oval.set(l, t, l + s - 2 * pad, t + s - 2 * pad)
        arc.shader = SweepGradient(w / 2f, h / 2f, intArrayOf(C.teal, C.gold, C.teal), null)
    }

    init {
        if (C.style != AppStyle.CLASSIC) setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    private val fx = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Plastischer bzw. leuchtender Untergrund des Rings je nach Stil. */
    private fun drawBase(c: Canvas, cx: Float, cy: Float, r: Float) {
        fx.reset(); fx.isAntiAlias = true
        when (C.style) {
            AppStyle.GLASS -> {
                fx.shader = android.graphics.RadialGradient(cx - r * 0.3f, cy - r * 0.4f, r * 1.3f,
                    alpha(Color.WHITE, if (C.light) 150 else 34), alpha(Color.WHITE, if (C.light) 40 else 4), Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, r - stroke, fx)
            }
            AppStyle.SOFT3D -> {
                val base = C.bgTop; val off = stroke * 1.3f
                fx.maskFilter = android.graphics.BlurMaskFilter(stroke * 2f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                fx.color = darker(base, if (C.light) 0.2f else 0.5f); c.drawCircle(cx + off, cy + off, r + stroke, fx)
                fx.color = lighter(base, if (C.light) 0.8f else 0.12f); c.drawCircle(cx - off, cy - off, r + stroke, fx)
                fx.maskFilter = null
                fx.color = base; c.drawCircle(cx, cy, r + stroke, fx)
                // eingedrückte Innenfläche
                fx.style = Paint.Style.STROKE; fx.strokeWidth = stroke * 0.8f
                fx.shader = LinearGradient(cx - r, cy - r, cx + r, cy + r,
                    darker(base, if (C.light) 0.15f else 0.45f), lighter(base, if (C.light) 0.7f else 0.1f), Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, r - stroke * 2.2f, fx)
            }
            AppStyle.GLOW -> {
                fx.shader = android.graphics.RadialGradient(cx, cy - r * 0.3f, r * 1.2f,
                    lighter(C.bgTop, if (C.light) 0.5f else 0.12f), C.bgTop, Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, r, fx)
                fx.shader = null
                fx.style = Paint.Style.STROKE; fx.strokeWidth = stroke * 3f
                fx.color = alpha(C.gold, 45)
                fx.maskFilter = android.graphics.BlurMaskFilter(stroke * 4f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                c.drawCircle(cx, cy, r, fx)
            }
            else -> {}
        }
    }

    override fun onDraw(canvas: Canvas) {
        val cx = oval.centerX(); val cy = oval.centerY(); val r = oval.width() / 2
        drawBase(canvas, cx, cy, r)
        if (C.style == AppStyle.GLASS || C.style == AppStyle.GLOW) {
            arc.setShadowLayer(stroke * (if (C.style == AppStyle.GLOW) 2.5f else 1.4f), 0f, 0f, C.gold)
        } else arc.clearShadowLayer()
        if (C.style == AppStyle.SOFT3D) { arc.shader = null; arc.color = C.gold }
        // feine Stundenmarken
        for (i in 0 until (if (C.style == AppStyle.CLASSIC || C.style == AppStyle.GLASS) 60 else 0)) {
            val a = Math.toRadians(i * 6.0)
            val inner = r - stroke * (if (i % 5 == 0) 2.6f else 1.8f)
            val outer = r - stroke * 1.2f
            canvas.drawLine(
                cx + (inner * cos(a)).toFloat(), cy + (inner * sin(a)).toFloat(),
                cx + (outer * cos(a)).toFloat(), cy + (outer * sin(a)).toFloat(), ticks
            )
        }
        canvas.drawArc(oval, 0f, 360f, false, track)
        canvas.save()
        canvas.rotate(-90f, cx, cy)
        canvas.drawArc(oval, 0f, 360f * progress, false, arc)
        val a = Math.toRadians(360.0 * progress)
        val px = cx + (r * cos(a)).toFloat(); val py = cy + (r * sin(a)).toFloat()
        canvas.drawCircle(px, py, stroke * 1.6f, glow)
        canvas.drawCircle(px, py, stroke * 0.9f, dot)
        canvas.restore()
    }
}

/** Kleine Trennlinie mit Ornament. */
fun Context.divider(): View = object : View(this) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = alpha(C.gold, 60); strokeWidth = dp(1f).toFloat() }
    private val d = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = C.gold }
    override fun onDraw(c: Canvas) {
        val cy = height / 2f; val cx = width / 2f; val s = dp(4f).toFloat()
        c.drawLine(width * 0.2f, cy, cx - s * 3, cy, p)
        c.drawLine(cx + s * 3, cy, width * 0.8f, cy, p)
        val path = Path().apply { moveTo(cx, cy - s); lineTo(cx + s, cy); lineTo(cx, cy + s); lineTo(cx - s, cy); close() }
        c.drawPath(path, d)
    }
}.apply { layoutParams = lp(h = dp(16), top = dp(4), bottom = dp(4)) }
