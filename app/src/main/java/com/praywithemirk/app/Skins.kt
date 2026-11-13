package com.praywithemirk.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Art der Hauptgrafik (trägt Countdown und nächstes Gebet). */
enum class Hero { RING, ARCH, SUNARC, TYPO, MOON, LANTERN, DIAL24, DISC, FRAME, FLAP, SOFT, KISWA }

/** Dekoration des Hintergrunds. */
enum class Deco { NONE, PATTERN, STARS, EMBERS, LEAVES, WOOD, GRAIN, ARCADES, MOSAIC, KISWA, GLOW, SKY }

/** Darstellung der Gebetszeiten-Liste. */
enum class ListStyle { ROWS, TILES, ARCH_TILES, FLAP }

/**
 * Ein komplettes Design („Skin“) für die App.
 * [heroBg]/[heroBg2] = Füllung der Hauptgrafik, [heroFg] = Text darin, [alt] = Zweitakzent.
 */
class Skin(
    val id: String,
    val title: T,
    val desc: T,
    val p: Palette,
    val hero: Hero,
    val deco: Deco,
    val list: ListStyle,
    val heroBg: String,
    val heroBg2: String,
    val heroFg: String,
    val alt: String,
    val serif: Boolean = false,
    val mono: Boolean = false,
)

object Skins {
    private fun pal(
        bgTop: String, bgBottom: String, card: String, cardStroke: String, accent: String, accentSoft: String,
        onAccent: String, text: String, muted: String, faint: String, secondary: String, light: Boolean,
        pattern: String = "#00000000",
    ) = Palette(bgTop, bgBottom, card, cardStroke, accent, accentSoft, onAccent, text, muted, faint, secondary, pattern, light)

    val all: List<Skin> = listOf(
        Skin("mihrab", T("Mihrab", "Mihrap", "Mihrab"),
            T("Grüne Gebetsnische mit Lampe, Gebetszeiten als kleine Bögen.", "Kandilli yeşil mihrap, vakitler küçük kemerler.", "Green prayer niche with lamp, times as small arches."),
            pal("#F5EFE2", "#EEE5D0", "#FFFFFFFF", "#66C9A253", "#A8852F", "#33C9A253", "#FFFFFF", "#1D3B2F", "#6B7D72", "#A9B3A8", "#2A5A46", true),
            Hero.ARCH, Deco.NONE, LS.ARCH, "#2A5A46", "#163A2C", "#F5EFE2", "#E8C46E"),
        Skin("sky", T("Himmelsbogen", "Gökyüzü yayı", "Sky arc"),
            T("Der Tag als Sonnenbahn, die Gebete als Stationen.", "Gün güneşin yolu, vakitler duraklar.", "The day as the sun's path, prayers as stations."),
            pal("#F7B49A", "#A9C9DE", "#B3FFFFFF", "#33FFFFFF", "#C2482A", "#33C2482A", "#FFFFFF", "#2B2335", "#5C5468", "#8E8899", "#E0603A", true),
            Hero.SUNARC, Deco.SKY, LS.ROWS, "#00000000", "#00000000", "#2B2335", "#FFF4C2"),
        Skin("zellige", T("Zellige", "Zellige", "Zellige"),
            T("Marokkanisches Mosaik, kräftige Fliesenfarben, dicke Ziffern.", "Fas mozaiği, canlı çini renkleri.", "Moroccan mosaic, bold tile colours."),
            pal("#0E3B4F", "#0B3040", "#FF1A5470", "#FFF2C14E", "#F2C14E", "#55F2C14E", "#0E3B4F", "#FDF6E9", "#B9D3DB", "#6E98A6", "#E76F51", false),
            Hero.FRAME, Deco.MOSAIC, LS.TILES, "#FDF6E9", "#FDF6E9", "#0E3B4F", "#E76F51"),
        Skin("typo", T("Typografie pur", "Saf tipografi", "Pure typography"),
            T("Plakat-Stil: riesige fette Ziffern, ein roter Akzent.", "Afiş tarzı: dev kalın rakamlar, tek kırmızı vurgu.", "Poster style: huge bold digits, one red accent."),
            pal("#F4F1EA", "#EFEBE2", "#00000000", "#22000000", "#111111", "#14000000", "#FFFFFF", "#111111", "#777777", "#BBBBBB", "#D1452F", true),
            Hero.TYPO, Deco.NONE, LS.ROWS, "#00000000", "#00000000", "#111111", "#D1452F"),
        Skin("moon", T("Mondnacht", "Ay gecesi", "Moon night"),
            T("Der Mond nach Hijri-Datum, Sterne, ruhige Nacht.", "Hicri tarihe göre ay, yıldızlar.", "The moon by Hijri date, stars, calm night."),
            pal("#1E2A55", "#05070F", "#0DFFFFFF", "#22FFFFFF", "#FFE39A", "#26FFE39A", "#0B1026", "#E9ECFF", "#9AA3CC", "#5C6488", "#B9C3FF", false),
            Hero.MOON, Deco.STARS, LS.ROWS, "#F4EED5", "#BDB59A", "#E9ECFF", "#B9C3FF"),
        Skin("lantern", T("Laterne", "Fener", "Lantern"),
            T("Eine glühende Laterne trägt den Countdown.", "Parlayan bir fener geri sayımı taşır.", "A glowing lantern carries the countdown."),
            pal("#3A220E", "#0D0805", "#FF24150A", "#44E9B670", "#E9B670", "#33FFB54A", "#1C1009", "#FFE9C4", "#C9A77D", "#7D6448", "#FFCF7A", false),
            Hero.LANTERN, Deco.GLOW, LS.ROWS, "#FFD27A", "#7A3D10", "#3A1B05", "#E58A2A"),
        Skin("bento", T("Bento", "Bento", "Bento"),
            T("Modernes Kachel-Raster wie Handy-Widgets.", "Telefon widget'ları gibi modern kutular.", "Modern tile grid like phone widgets."),
            pal("#EEF1F6", "#E6EAF2", "#FFFFFFFF", "#141D2A44", "#3B5BDB", "#263B5BDB", "#FFFFFF", "#1D2A44", "#6B7690", "#ADB5BD", "#087F5B", true),
            Hero.FRAME, Deco.NONE, LS.TILES, "#3B5BDB", "#7048E8", "#FFFFFF", "#FFFFFF"),
        Skin("dial", T("24-Stunden-Zifferblatt", "24 saatlik kadran", "24-hour dial"),
            T("Alle Gebete auf einem Tages-Zifferblatt, Zeiger = jetzt.", "Tüm vakitler günlük kadranda.", "All prayers on a day dial, hand = now."),
            pal("#1F3060", "#0B1328", "#10FFFFFF", "#22FFFFFF", "#F2C14E", "#26F2C14E", "#14213D", "#FFFFFF", "#A9B4D0", "#5C6A8E", "#FF8A5B", false),
            Hero.DIAL24, Deco.NONE, LS.ROWS, "#00000000", "#00000000", "#FFFFFF", "#FF8A5B"),
        Skin("kiswa", T("Kiswa", "Kisve", "Kiswa"),
            T("Schwarz und Gold, Kalligrafie-Fries, edle dünne Ziffern.", "Siyah ve altın, hat frizi.", "Black and gold, calligraphy frieze."),
            pal("#12100C", "#0B0A08", "#14FFFFFF", "#66C9A14A", "#E6C473", "#22E6C473", "#0B0A08", "#FFF3D6", "#C9A14A", "#7A6A48", "#C9A14A", false),
            Hero.KISWA, Deco.KISWA, LS.ROWS, "#00000000", "#00000000", "#FFF3D6", "#E6C473"),
        Skin("andalus", T("Andalus", "Endülüs", "Andalus"),
            T("Hell wie ein Alhambra-Innenhof: Arkaden, Bogen, Brunnen.", "Elhamra avlusu gibi aydınlık.", "Bright like an Alhambra courtyard."),
            pal("#FDF7EE", "#E9F2EE", "#FFFFFFFF", "#33C8A774", "#B5793A", "#2A2F4A45", "#FFFFFF", "#2F4A45", "#6F7F7A", "#AAB5B0", "#7FB8A8", true),
            Hero.ARCH, Deco.ARCADES, LS.ARCH, "#FFFFFF", "#D7ECE5", "#2F4A45", "#7FB8A8", serif = false),
        Skin("board", T("Abfahrtstafel", "Kalkış panosu", "Departure board"),
            T("Wie die Fallblatt-Anzeige im Bahnhof.", "Gardaki döner tabela gibi.", "Like the split-flap board at the station."),
            pal("#0D0D0D", "#0D0D0D", "#FF181818", "#FF222222", "#F5D547", "#402A250A", "#0D0D0D", "#FFFFFF", "#AAAAAA", "#555555", "#7CFC7C", false),
            Hero.FLAP, Deco.NONE, LS.FLAP, "#181818", "#222222", "#FFFFFF", "#F5D547", mono = true),
        Skin("fire", T("Kaminabend", "Şömine akşamı", "Fireside"),
            T("Warme Glut, schwebende Funken, leicht glimmende Ziffern.", "Sıcak kor, uçuşan kıvılcımlar.", "Warm embers, floating sparks."),
            pal("#1E1511", "#151110", "#08FFFFFF", "#22F0A868", "#F0A868", "#26F0A868", "#1E1511", "#F3E3CF", "#B39A82", "#6E5A48", "#C0582A", false),
            Hero.SOFT, Deco.EMBERS, LS.ROWS, "#00000000", "#00000000", "#F3E3CF", "#F0A868", serif = true),
        Skin("walnut", T("Walnuss & Messing", "Ceviz ve pirinç", "Walnut & brass"),
            T("Wie eine Holz-Wanduhr: Maserung, Zifferblatt im Messingring.", "Ahşap duvar saati gibi.", "Like a wooden wall clock."),
            pal("#3B2A1E", "#32241A", "#CC1F1610", "#44D4B072", "#D4B072", "#30D4B072", "#3B2A1E", "#EFE2CC", "#B89F80", "#7A6450", "#B48C4C", false),
            Hero.DISC, Deco.WOOD, LS.ROWS, "#F4EAD6", "#E2D3B6", "#3B2A1E", "#B48C4C", serif = true),
        Skin("moss", T("Moos", "Yosun", "Moss"),
            T("Tiefes Waldgrün mit zarten Blättern, sehr beruhigend.", "Koyu orman yeşili, narin yapraklar.", "Deep forest green with delicate leaves."),
            pal("#26372A", "#141D17", "#07FFFFFF", "#22B8CF9A", "#B8CF9A", "#22B8CF9A", "#141D17", "#E4EAD8", "#9FB197", "#5F6F5A", "#7FA36A", false),
            Hero.SOFT, Deco.LEAVES, LS.ROWS, "#00000000", "#00000000", "#E4EAD8", "#B8CF9A", serif = true),
        Skin("andalus_night", T("Andalus bei Nacht", "Gece Endülüs", "Andalus at night"),
            T("Der Innenhof bei Nacht: Laterne, Brunnen, Goldakzente.", "Gece avlu: fener, çeşme, altın.", "The courtyard at night: lantern, fountain, gold."),
            pal("#14233A", "#1D3436", "#08FFFFFF", "#22E9C27A", "#E9C27A", "#24E9C27A", "#14233A", "#EFE6D6", "#A7B3B8", "#5F6B78", "#5E9A95", false),
            Hero.ARCH, Deco.ARCADES, LS.ARCH, "#2C3F55", "#1A2A3D", "#EFE6D6", "#E9C27A", serif = true),
        Skin("ink", T("Tinte & Papier", "Mürekkep ve kâğıt", "Ink & paper"),
            T("Wie eine Buchseite bei Leselicht: Serifenschrift, viel Ruhe.", "Okuma ışığında kitap sayfası gibi.", "Like a book page under a reading lamp."),
            pal("#232A3B", "#151922", "#00000000", "#00000000", "#C9B48A", "#0DFFFFFF", "#151922", "#E6E1D6", "#9A9FAE", "#5A5F6E", "#C9B48A", false),
            Hero.SOFT, Deco.GRAIN, LS.ROWS, "#00000000", "#00000000", "#E6E1D6", "#C9B48A", serif = true),
    )

    /** Kurzname für die Listenart (lesbarer in der Tabelle oben). */
    private object LS { val ARCH = ListStyle.ARCH_TILES; val ROWS = ListStyle.ROWS; val TILES = ListStyle.TILES; val FLAP = ListStyle.FLAP }

    fun byId(id: String?): Skin? = all.firstOrNull { it.id == id }
}

fun col(s: String): Int = Color.parseColor(s)

/** Schriften je Design. */
object SkinFonts {
    fun name(): Typeface = when {
        C.skin?.mono == true -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        C.skin?.serif == true -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        else -> Fonts.medium
    }
    fun nameBold(): Typeface = when {
        C.skin?.mono == true -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        C.skin?.serif == true -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
        else -> Fonts.bold
    }
    fun digits(): Typeface = when {
        C.skin?.mono == true -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        C.skin?.hero == Hero.TYPO || C.skin?.hero == Hero.FRAME -> Typeface.create("sans-serif", Typeface.BOLD)
        C.skin?.hero == Hero.DISC -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
        C.skin?.hero == Hero.KISWA || C.skin?.hero == Hero.SOFT || C.skin?.hero == Hero.SUNARC -> Fonts.light
        C.skin != null -> Fonts.medium
        else -> Fonts.medium
    }
    fun body(): Typeface = when {
        C.skin?.serif == true -> Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        else -> Fonts.regular
    }
}

/**
 * Zeichnet die Hauptgrafik eines Designs (Bogen, Sonnenbahn, Mond, Laterne, Zifferblatt …).
 * [progress] = Anteil der Zeit zwischen letztem und nächstem Gebet,
 * [nowHour] = aktuelle Uhrzeit in Stunden, [times] = Gebetszeiten in Stunden (6 Werte).
 */
class HeroArtView(context: Context, private val skin: Skin) : View(context) {
    var progress = 0f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }
    var nowHour = 12f
    var times = floatArrayOf(5.6f, 7.5f, 12.1f, 14.1f, 16.5f, 18.3f)
    var nextIndex = 3

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val r = RectF()

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) } // klein, für Unschärfe/Glühen

    private fun d(v: Float) = context.dp(v).toFloat()

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); val s = min(w, h)
        val cx = w / 2; val cy = h / 2
        p.reset(); p.isAntiAlias = true
        when (skin.hero) {
            Hero.ARCH -> {
                val left = w * 0.06f; val right = w * 0.94f; val top = h * 0.03f; val bottom = h * 0.97f
                val rad = (right - left) / 2
                path.reset()
                path.moveTo(left, bottom - d(16f)); path.lineTo(left, top + rad)
                path.arcTo(RectF(left, top, right, top + 2 * rad), 180f, 180f)
                path.lineTo(right, bottom - d(16f)); path.quadTo(right, bottom, right - d(16f), bottom)
                path.lineTo(left + d(16f), bottom); path.quadTo(left, bottom, left, bottom - d(16f)); path.close()
                p.shader = RadialGradient(cx, top + rad * 0.8f, s * 0.8f, col(skin.heroBg), col(skin.heroBg2), Shader.TileMode.CLAMP)
                p.setShadowLayer(d(10f), 0f, d(4f), Color.argb(70, 0, 0, 0))
                c.drawPath(path, p)
                p.clearShadowLayer(); p.shader = null
                p.style = Paint.Style.STROKE; p.strokeWidth = d(2f); p.color = alpha(C.gold, 170)
                c.drawPath(path, p)
                // Lampe
                p.style = Paint.Style.FILL; p.color = col(skin.alt)
                c.drawLine(cx, top + d(6f), cx, top + s * 0.1f, p.apply { strokeWidth = d(1.5f) })
                p.setShadowLayer(d(14f), 0f, 0f, col(skin.alt))
                c.drawRoundRect(cx - s * 0.035f, top + s * 0.1f, cx + s * 0.035f, top + s * 0.17f, d(4f), d(4f), p)
                p.clearShadowLayer()
                // Fortschritt als Linie unten
                val bw = s * 0.42f; val by = bottom - s * 0.1f
                p.color = alpha(col(skin.heroFg), 50); c.drawRoundRect(cx - bw / 2, by, cx + bw / 2, by + d(5f), d(3f), d(3f), p)
                p.color = col(skin.alt); c.drawRoundRect(cx - bw / 2, by, cx - bw / 2 + bw * progress, by + d(5f), d(3f), d(3f), p)
            }
            Hero.SUNARC -> {
                val baseY = h * 0.62f; val l = w * 0.06f; val rr = w * 0.94f; val topY = h * 0.08f
                fun pt(t: Float): Pair<Float, Float> { // t 0..1 entlang Bogen (quadratische Kurve)
                    val x = (1 - t) * (1 - t) * l + 2 * (1 - t) * t * cx + t * t * rr
                    val y = (1 - t) * (1 - t) * baseY + 2 * (1 - t) * t * (topY - (baseY - topY) * 0.6f) + t * t * baseY
                    return x to y
                }
                val start = times[0]; val end = times[5]
                fun tOf(hh: Float) = ((hh - start) / (end - start)).coerceIn(0f, 1f)
                p.style = Paint.Style.STROKE; p.strokeWidth = d(2f); p.color = alpha(col(skin.heroFg), 60)
                p.pathEffect = android.graphics.DashPathEffect(floatArrayOf(d(4f), d(6f)), 0f)
                path.reset(); var first = true
                for (i in 0..60) { val (x, y) = pt(i / 60f); if (first) path.moveTo(x, y) else path.lineTo(x, y); first = false }
                c.drawPath(path, p); p.pathEffect = null
                p.strokeWidth = d(3f); p.color = col(skin.heroFg)
                path.reset(); first = true
                val tn = tOf(nowHour)
                for (i in 0..60) { val t = tn * i / 60f; val (x, y) = pt(t); if (first) path.moveTo(x, y) else path.lineTo(x, y); first = false }
                c.drawPath(path, p)
                p.style = Paint.Style.FILL
                c.drawLine(0f, baseY, w, baseY, p.apply { color = alpha(col(skin.heroFg), 70); strokeWidth = d(1.5f) })
                val (sx, sy) = pt(tn)
                p.color = alpha(col(skin.alt), 110); c.drawCircle(sx, sy, s * 0.09f, p)
                p.color = col(skin.alt); c.drawCircle(sx, sy, s * 0.055f, p)
                for (i in 0 until 6) {
                    val (x, y) = pt(tOf(times[i]))
                    p.color = if (i == nextIndex) C.gold else col(skin.heroFg)
                    c.drawCircle(x, y, if (i == nextIndex) d(6f) else d(4f), p)
                }
            }
            Hero.MOON -> {
                // Mondphase aus dem Hijri-Tag (1 = schmale Sichel, 15 = voll)
                val day = try { java.time.chrono.HijrahDate.now().get(java.time.temporal.ChronoField.DAY_OF_MONTH) } catch (_: Exception) { 3 }
                val phase = (day / 29.5f).coerceIn(0f, 1f) // 0..1
                val mr = s * 0.4f
                p.color = Color.argb(40, 255, 246, 210); c.drawCircle(cx, cy, mr * 1.25f, p)
                p.shader = RadialGradient(cx + mr * 0.2f, cy - mr * 0.3f, mr * 1.3f, col(skin.heroBg), col(skin.heroBg2), Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, mr, p); p.shader = null
                // Schatten: verschobener Kreis in Hintergrundfarbe (zunehmend = von links beschattet)
                val waxing = phase < 0.5f
                val k = if (waxing) phase * 2 else (1 - phase) * 2 // 0 = neu, 1 = voll
                val off = mr * 2 * k
                p.color = C.bgTop
                c.save()
                path.reset(); path.addCircle(cx, cy, mr, Path.Direction.CW); c.clipPath(path)
                c.drawCircle(if (waxing) cx - off else cx + off, cy, mr * 1.02f, p)
                c.restore()
            }
            Hero.LANTERN -> {
                val glowA = (90 + 120 * progress).toInt().coerceIn(0, 230)
                p.shader = RadialGradient(cx, cy, s * 0.55f, Color.argb(glowA, 255, 181, 74), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, s * 0.55f, p); p.shader = null
                val bw = s * 0.62f; val top = s * 0.12f; val bot = s * 0.9f
                p.color = col("#B8792C"); c.drawLine(cx, 0f, cx, top, p.apply { strokeWidth = d(3f) })
                path.reset()
                path.moveTo(cx - bw * 0.32f, top); path.lineTo(cx + bw * 0.32f, top); path.lineTo(cx + bw * 0.5f, top + s * 0.18f)
                path.lineTo(cx + bw * 0.5f, bot - s * 0.18f); path.lineTo(cx + bw * 0.32f, bot); path.lineTo(cx - bw * 0.32f, bot)
                path.lineTo(cx - bw * 0.5f, bot - s * 0.18f); path.lineTo(cx - bw * 0.5f, top + s * 0.18f); path.close()
                p.shader = RadialGradient(cx, cy, bw * 0.75f, col(skin.heroBg), col(skin.heroBg2), Shader.TileMode.CLAMP)
                c.drawPath(path, p); p.shader = null
                p.style = Paint.Style.STROKE; p.strokeWidth = d(3f); p.color = col("#8A5216")
                c.drawPath(path, p)
                c.drawLine(cx, top, cx, bot, p.apply { alpha = 120 })
            }
            Hero.DIAL24 -> {
                val rr = s * 0.44f
                fun pt(hh: Float, rad: Float): Pair<Float, Float> { val a = Math.toRadians(hh / 24.0 * 360 + 90); return (cx + rad * cos(a)).toFloat() to (cy + rad * sin(a)).toFloat() }
                p.style = Paint.Style.STROKE; p.strokeWidth = s * 0.06f; p.color = Color.argb(30, 255, 255, 255)
                c.drawCircle(cx, cy, rr, p)
                r.set(cx - rr, cy - rr, cx + rr, cy + rr)
                fun ang(hh: Float) = hh / 24f * 360f + 90f
                p.color = alpha(C.gold, 70); c.drawArc(r, ang(times[1]), (times[4] - times[1]) / 24f * 360f, false, p)
                p.strokeWidth = d(1.5f); p.color = Color.argb(80, 255, 255, 255)
                for (hh in 0 until 24) { val (x1, y1) = pt(hh.toFloat(), rr * 0.8f); val (x2, y2) = pt(hh.toFloat(), rr * (if (hh % 6 == 0) 0.74f else 0.84f)); c.drawLine(x1, y1, x2, y2, p) }
                p.color = alpha(C.gold, 60); p.strokeWidth = d(1.5f)
                val sq = s * 0.17f
                for (k in listOf(0f, 45f)) { c.save(); c.rotate(k, cx, cy); c.drawRect(cx - sq, cy - sq, cx + sq, cy + sq, p); c.restore() }
                p.style = Paint.Style.FILL
                for (i in 0 until 6) {
                    val (x, y) = pt(times[i], rr)
                    p.color = if (i == nextIndex) C.teal else Color.WHITE
                    c.drawCircle(x, y, if (i == nextIndex) s * 0.03f else s * 0.018f, p)
                }
                val (hx, hy) = pt(nowHour, rr * 0.92f)
                p.color = C.gold; p.strokeWidth = s * 0.012f; p.strokeCap = Paint.Cap.ROUND
                c.drawLine(cx, cy, hx, hy, p); c.drawCircle(cx, cy, s * 0.02f, p)
            }
            Hero.DISC -> {
                val dr = s * 0.45f
                p.setShadowLayer(d(14f), 0f, d(8f), Color.argb(140, 0, 0, 0))
                p.color = col(skin.alt); c.drawCircle(cx, cy, dr, p); p.clearShadowLayer()
                p.color = darker(col(skin.alt), 0.35f); c.drawCircle(cx, cy, dr * 0.965f, p)
                p.shader = RadialGradient(cx - dr * 0.2f, cy - dr * 0.3f, dr * 1.2f, col(skin.heroBg), col(skin.heroBg2), Shader.TileMode.CLAMP)
                c.drawCircle(cx, cy, dr * 0.93f, p); p.shader = null
                p.color = alpha(col(skin.heroFg), 70); p.strokeWidth = d(1.5f)
                for (i in 0 until 60) {
                    val a = Math.toRadians(i * 6.0); val r1 = dr * 0.88f; val r2 = dr * (if (i % 5 == 0) 0.8f else 0.85f)
                    c.drawLine(cx + (r1 * cos(a)).toFloat(), cy + (r1 * sin(a)).toFloat(), cx + (r2 * cos(a)).toFloat(), cy + (r2 * sin(a)).toFloat(), p)
                }
            }
            Hero.FRAME -> {
                val rad = d(if (skin.id == "zellige") 8f else 26f)
                r.set(d(4f), d(4f), w - d(4f), h - d(10f))
                if (skin.id == "zellige") {
                    p.color = col(skin.alt); c.drawRoundRect(r.left, r.top + d(8f), r.right, r.bottom + d(8f), rad, rad, p)
                    p.color = C.gold; c.drawRoundRect(r, rad, rad, p)
                    p.color = col(skin.heroBg); c.drawRoundRect(r.left + d(8f), r.top + d(8f), r.right - d(8f), r.bottom - d(8f), rad, rad, p)
                } else {
                    p.shader = LinearGradient(r.left, r.top, r.right, r.bottom, col(skin.heroBg), col(skin.heroBg2), Shader.TileMode.CLAMP)
                    p.setShadowLayer(d(12f), 0f, d(6f), Color.argb(60, 30, 40, 90))
                    c.drawRoundRect(r, rad, rad, p); p.shader = null; p.clearShadowLayer()
                }
                val bw = (r.width()) * 0.7f; val by = r.bottom - s * 0.1f
                p.color = alpha(col(skin.heroFg), 50); c.drawRoundRect(cx - bw / 2, by, cx + bw / 2, by + d(6f), d(3f), d(3f), p)
                p.color = if (skin.id == "zellige") col(skin.alt) else col(skin.heroFg)
                c.drawRoundRect(cx - bw / 2, by, cx - bw / 2 + bw * progress, by + d(6f), d(3f), d(3f), p)
            }
            Hero.FLAP -> {
                r.set(d(2f), d(2f), w - d(2f), h - d(2f))
                p.color = col(skin.heroBg); c.drawRoundRect(r, d(12f), d(12f), p)
            }
            Hero.TYPO, Hero.SOFT -> {
                // nur Fortschrittsbalken unten
                val bw = w * (if (skin.hero == Hero.TYPO) 0.9f else 0.6f); val by = h * 0.86f
                val x0 = if (skin.hero == Hero.TYPO) w * 0.05f else cx - bw / 2
                val th = if (skin.hero == Hero.TYPO) d(6f) else d(3f)
                p.color = alpha(col(skin.heroFg), 35); c.drawRoundRect(x0, by, x0 + bw, by + th, th, th, p)
                p.color = if (skin.hero == Hero.TYPO) col(skin.heroFg) else col(skin.alt)
                if (skin.hero == Hero.SOFT) p.setShadowLayer(d(8f), 0f, 0f, col(skin.alt))
                c.drawRoundRect(x0, by, x0 + bw * progress, by + th, th, th, p); p.clearShadowLayer()
                if (skin.id == "ink") { // kleines Ornament
                    p.color = alpha(C.gold, 140); p.textAlign = Paint.Align.CENTER; p.textSize = s * 0.08f
                    c.drawText("❦", cx, by + s * 0.1f, p)
                }
            }
            Hero.KISWA -> {
                p.color = alpha(C.gold, 90); p.strokeWidth = d(1f)
                c.drawLine(w * 0.15f, h * 0.9f, w * 0.85f, h * 0.9f, p)
                p.color = C.gold; c.drawCircle(cx, h * 0.9f, d(3f), p)
            }
            Hero.RING -> {}
        }
    }
}
