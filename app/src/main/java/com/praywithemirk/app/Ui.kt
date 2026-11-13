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

/** Aktive Farben – werden beim Start jeder Seite aus dem gewählten Design gesetzt. */
object C {
    var bgTop = 0; var bgBottom = 0; var card = 0; var cardStroke = 0
    var gold = 0; var goldSoft = 0; var onAccent = 0
    var cream = 0; var muted = 0; var faint = 0; var teal = 0; var pattern = 0
    var light = false
    var theme = AppTheme.NIGHT

    init { use(AppTheme.NIGHT) }

    fun use(t: AppTheme) {
        theme = t
        val p = t.p
        fun c(s: String) = Color.parseColor(s)
        bgTop = c(p.bgTop); bgBottom = c(p.bgBottom); card = c(p.card); cardStroke = c(p.cardStroke)
        gold = c(p.accent); goldSoft = c(p.accentSoft); onAccent = c(p.onAccent)
        cream = c(p.text); muted = c(p.muted); faint = c(p.faint); teal = c(p.secondary)
        pattern = c(p.pattern); light = p.light
    }

    /** Design anwenden und Fensterfarben/Statusleiste anpassen. */
    @Suppress("DEPRECATION")
    fun applyTo(a: android.app.Activity, t: AppTheme) {
        use(t)
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
    background = roundRect(C.card, dp(20).toFloat(), C.cardStroke, dp(1))
    setPadding(dp(padding), dp(padding), dp(padding), dp(padding))
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

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)
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

    private val stroke = context.dp(6f).toFloat()
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
        val pad = stroke * 2.2f
        val s = min(w, h).toFloat()
        val l = (w - s) / 2 + pad
        val t = (h - s) / 2 + pad
        oval.set(l, t, l + s - 2 * pad, t + s - 2 * pad)
        arc.shader = SweepGradient(w / 2f, h / 2f, intArrayOf(C.teal, C.gold, C.teal), null)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = oval.centerX(); val cy = oval.centerY(); val r = oval.width() / 2
        // feine Stundenmarken
        for (i in 0 until 60) {
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
