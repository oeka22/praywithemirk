package com.praywithemirk.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Orientalische Schmuckelemente für die Designs „Fanus“ und „Mashrabiya“:
 * achtzackige Sterne (Girih-Muster), hängende Laternen, Gitterband, Spitzbogen, warmes Licht.
 */
object Orient {

    /** Achtzackiger Stern als Pfad (abwechselnd äußerer/innerer Radius). */
    fun star8(path: Path, cx: Float, cy: Float, r: Float, inner: Float = r * 0.62f) {
        path.reset()
        for (i in 0 until 16) {
            val a = Math.PI / 8 * i - Math.PI / 2
            val rr = if (i % 2 == 0) r else inner
            val x = cx + (rr * cos(a)).toFloat()
            val y = cy + (rr * sin(a)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }

    /** Feines Girih-Muster: Sternumrisse mit Verbindungslinien, über die ganze Fläche. */
    fun drawGirih(c: Canvas, w: Float, h: Float, tile: Float, color: Int, stroke: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = stroke; this.color = color }
        val path = Path()
        val r = tile * 0.26f
        var y = 0f
        while (y < h + tile) {
            var x = 0f
            while (x < w + tile) {
                val cx = x + tile / 2; val cy = y + tile / 2
                star8(path, cx, cy, r)
                c.drawPath(path, p)
                c.drawLine(x, cy, cx - r, cy, p); c.drawLine(cx + r, cy, x + tile, cy, p)
                c.drawLine(cx, y, cx, cy - r, p); c.drawLine(cx, cy + r, cx, y + tile, p)
                x += tile
            }
            y += tile
        }
    }

    /** Warmer Lichtschein (radial). */
    fun glow(c: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = RadialGradient(cx, cy, max(r, 1f), color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r, p)
    }

    /** Hängende Laterne (Fanus) mit Kette und Lichtschein. [top] = Oberkante des Laternenkörpers. */
    fun drawLantern(c: Canvas, cx: Float, top: Float, scale: Float, lamp: Int, accent: Int, dp1: Float, halo: Boolean = true) {
        val w = 26f * scale * dp1
        val h = 44f * scale * dp1
        if (halo) glow(c, cx, top + h * 0.55f, 70f * scale * dp1, alpha(lamp, 120))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = accent; p.strokeWidth = 1.5f * dp1
        c.drawLine(cx, 0f, cx, top, p)
        val body = Path().apply {
            moveTo(cx - w * 0.5f, top + h * 0.22f)
            lineTo(cx - w * 0.32f, top + h * 0.92f)
            lineTo(cx + w * 0.32f, top + h * 0.92f)
            lineTo(cx + w * 0.5f, top + h * 0.22f)
            close()
        }
        p.style = Paint.Style.FILL; p.color = alpha(lamp, 220)
        c.drawPath(body, p)
        p.style = Paint.Style.STROKE; p.color = accent; p.strokeWidth = 1.6f * dp1
        c.drawPath(body, p)
        c.drawLine(cx, top + h * 0.22f, cx, top + h * 0.92f, p)
        p.style = Paint.Style.FILL
        val cap = Path().apply {
            moveTo(cx - w * 0.42f, top + h * 0.22f)
            quadTo(cx, top - h * 0.08f, cx + w * 0.42f, top + h * 0.22f)
            close()
        }
        c.drawPath(cap, p)
        val foot = Path().apply {
            moveTo(cx - w * 0.18f, top + h * 0.92f); lineTo(cx, top + h * 1.08f); lineTo(cx + w * 0.18f, top + h * 0.92f); close()
        }
        c.drawPath(foot, p)
        val star = Path(); star8(star, cx, top + h * 0.56f, w * 0.2f)
        p.color = Color.parseColor("#FFF3D6")
        c.drawPath(star, p)
    }
}

/** Fortschrittsleiste aus kleinen Quadraten: füllt sich von links nach rechts und leuchtet, das vorderste am hellsten. */
class SegmentBar(context: Context, private val stars: Boolean) : View(context) {
    var progress = 0f
        set(v) { val n = v.coerceIn(0f, 1f); if (n != field) { field = n; invalidate() } }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val r = RectF()

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) } // schmale Leiste: Leuchten per Schatten

    private val star = Path()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0f) return
        val pad = h * 0.25f // Platz fürs Leuchten
        val sq = h - pad * 2
        val gap = sq * 0.4f
        val ends = if (stars) sq * 1.8f else 0f
        if (stars) {
            p.clearShadowLayer(); p.color = alpha(C.gold, 220)
            Orient.star8(star, ends * 0.45f, h / 2, sq * 0.62f); c.drawPath(star, p)
            Orient.star8(star, w - ends * 0.45f, h / 2, sq * 0.62f); c.drawPath(star, p)
        }
        val x0 = pad + ends
        val avail = w - 2 * x0
        val n = max(8, ((avail + gap) / (sq + gap)).toInt())
        val step = (avail + gap) / n
        val lit = (progress * n).roundToInt()
        for (i in 0 until n) {
            val x = x0 + i * step
            r.set(x, pad, x + step - gap, pad + sq)
            val on = i < lit
            if (on) {
                val a = (0.45f + 0.55f * (i + 1f) / max(lit, 1)).coerceAtMost(1f)
                p.color = alpha(C.gold, (a * 255).toInt())
                p.setShadowLayer(if (i == lit - 1) pad * 1.6f else pad * 0.7f, 0f, 0f, alpha(C.gold, if (i == lit - 1) 230 else 120))
            } else {
                p.color = alpha(C.cream, if (C.light) 40 else 36)
                p.clearShadowLayer()
            }
            c.drawRoundRect(r, sq * 0.18f, sq * 0.18f, p)
        }
        p.clearShadowLayer()
    }
}

/** Hintergrund der Timer-Zeile: warmer Lichtschein in der Mitte, optional Spitzbogen mit Stern (Mashrabiya). */
class TimerGlowDrawable(private val lamp: Int, private val accent: Int, private val arch: Boolean, private val dp1: Float, private val glow: Boolean = true) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c: Canvas) {
        val b = bounds
        val cx = b.exactCenterX(); val cy = b.exactCenterY()
        if (glow) Orient.glow(c, cx, cy, min(b.width() * 0.32f, b.height() * 1.4f), alpha(lamp, 95))
        if (!arch) return
        val aw = min(b.width() * 0.42f, b.height() * 2.3f)
        val l = cx - aw / 2; val r = cx + aw / 2
        val top = b.top + b.height() * 0.06f; val bottom = b.bottom.toFloat() + dp1 * 6
        fun arc(inset: Float): Path {
            val ll = l + inset; val rr = r - inset; val tt = top + inset
            val sp = tt + (bottom - tt) * 0.45f
            return Path().apply {
                moveTo(ll, bottom); lineTo(ll, sp)
                quadTo(ll, tt + (sp - tt) * 0.35f, cx, tt)
                quadTo(rr, tt + (sp - tt) * 0.35f, rr, sp)
                lineTo(rr, bottom)
            }
        }
        p.style = Paint.Style.STROKE
        p.color = alpha(accent, 90); p.strokeWidth = 2f * dp1
        c.drawPath(arc(0f), p)
        p.color = alpha(accent, 46); p.strokeWidth = 1f * dp1
        c.drawPath(arc(10f * dp1), p)
        p.style = Paint.Style.FILL; p.color = alpha(accent, 180)
        val s = Path(); Orient.star8(s, cx, top - 2f * dp1, 8f * dp1)
        c.drawPath(s, p)
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

/** Gitterband (Mashrabiya): Rauten mit kleinen Sternen, abgerundeter Rahmen. */
class LatticeDrawable(private val accent: Int, private val dp1: Float) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c: Canvas) {
        val b = bounds
        val rad = 20f * dp1
        val rect = RectF(b)
        val save = c.save()
        val clip = Path().apply { addRoundRect(rect, rad, rad, Path.Direction.CW) }
        c.clipPath(clip)
        val t = 28f * dp1
        val star = Path()
        var y = b.top.toFloat()
        while (y < b.bottom) {
            var x = b.left.toFloat()
            while (x < b.right) {
                p.style = Paint.Style.STROKE; p.strokeWidth = dp1; p.color = alpha(accent, 56)
                val d = Path().apply { moveTo(x + t / 2, y); lineTo(x + t, y + t / 2); lineTo(x + t / 2, y + t); lineTo(x, y + t / 2); close() }
                c.drawPath(d, p)
                p.style = Paint.Style.FILL; p.color = alpha(accent, 46)
                Orient.star8(star, x + t / 2, y + t / 2, 5f * dp1)
                c.drawPath(star, p)
                x += t
            }
            y += t
        }
        c.restoreToCount(save)
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f * dp1; p.color = alpha(accent, 76)
        c.drawRoundRect(rect.apply { inset(dp1, dp1) }, rad, rad, p)
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

/** Kleine Sternrosette oben rechts (z. B. in der Hadith-Box). */
class RosetteDrawable(private val accent: Int, private val size: Float, private val inset: Float) : Drawable() {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    override fun draw(c: Canvas) {
        val b = bounds
        p.color = alpha(accent, 205)
        Orient.star8(path, b.right - inset - size, b.top + inset + size, size)
        c.drawPath(path, p)
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
