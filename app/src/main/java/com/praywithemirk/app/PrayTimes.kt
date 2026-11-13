package com.praywithemirk.app

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

/** Indizes der sechs Zeiten, wie sie überall in der App verwendet werden. */
object P {
    const val FAJR = 0
    const val SUNRISE = 1
    const val DHUHR = 2
    const val ASR = 3
    const val MAGHRIB = 4
    const val ISHA = 5
    val ALL = intArrayOf(FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA)
    /** Gebete mit Gebetsruf (Sonnenaufgang hat keinen Adhan). */
    val SALAH = intArrayOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
}

/**
 * Berechnungsmethode.
 * @param fajr Sonnenwinkel unter dem Horizont für Fajr
 * @param isha Sonnenwinkel für Isha (wenn [ishaMinutes] == 0)
 * @param ishaMinutes feste Minuten nach Maghrib (z. B. Umm al-Qura)
 * @param adjust feste Korrekturen in Minuten je Zeit (Fajr, Shuruq, Dhuhr, Asr, Maghrib, Isha)
 */
enum class Method(
    val id: String,
    val label: String,
    val fajr: Double,
    val isha: Double,
    val ishaMinutes: Int = 0,
    val adjust: IntArray = intArrayOf(0, 0, 0, 0, 0, 0),
) {
    DIYANET("diyanet", "Diyanet (Türkiye)", 18.0, 17.0, adjust = intArrayOf(0, -7, 5, 4, 7, 0)),
    MWL("mwl", "Muslim World League", 18.0, 17.0),
    ISNA("isna", "ISNA (Nordamerika)", 15.0, 15.0),
    EGYPT("egypt", "Egyptian General Authority", 19.5, 17.5),
    MAKKAH("makkah", "Umm al-Qura (Makkah)", 18.5, 0.0, ishaMinutes = 90),
    KARACHI("karachi", "University of Karachi", 18.0, 18.0),
    UOIF("uoif", "UOIF (Frankreich, 12°)", 12.0, 12.0);

    companion object {
        fun byId(id: String?): Method = entries.firstOrNull { it.id == id } ?: DIYANET
    }
}

/** Regel für hohe Breiten (in Deutschland im Sommer wichtig). */
enum class HighLatRule(val id: String) {
    ANGLE_BASED("angle"), NIGHT_MIDDLE("middle"), ONE_SEVENTH("seventh"), NONE("none");

    companion object {
        fun byId(id: String?): HighLatRule = entries.firstOrNull { it.id == id } ?: ANGLE_BASED
    }
}

class CalcParams(
    val method: Method,
    val hanafiAsr: Boolean,
    val highLat: HighLatRule,
    /** Zusätzliche Benutzer-Korrektur in Minuten für jede der 6 Zeiten. */
    val userOffsets: IntArray,
)

/**
 * Astronomische Berechnung der Gebetszeiten
 * (Portierung des bewährten PrayTimes.org-Algorithmus, rein offline).
 */
object PrayTimes {

    private fun dtr(d: Double) = d * PI / 180.0
    private fun rtd(r: Double) = r * 180.0 / PI
    private fun dsin(d: Double) = sin(dtr(d))
    private fun dcos(d: Double) = cos(dtr(d))
    private fun dtan(d: Double) = tan(dtr(d))
    private fun darcsin(x: Double) = rtd(asin(x))
    private fun darccos(x: Double) = rtd(acos(x))
    private fun darctan2(y: Double, x: Double) = rtd(atan2(y, x))
    private fun darccot(x: Double) = rtd(atan(1.0 / x))
    private fun fix(a: Double, b: Double): Double {
        val r = a - b * floor(a / b)
        return if (r < 0) r + b else r
    }
    private fun fixAngle(a: Double) = fix(a, 360.0)
    private fun fixHour(a: Double) = fix(a, 24.0)
    private fun timeDiff(t1: Double, t2: Double) = fixHour(t2 - t1)

    private fun julian(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private class SunPos(val decl: Double, val eqt: Double)

    private fun sunPosition(jd: Double): SunPos {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = fixHour(darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0)
        val eqt = q / 15.0 - ra
        val decl = darcsin(dsin(e) * dsin(l))
        return SunPos(decl, eqt)
    }

    /**
     * Berechnet die sechs Zeiten für [date] am Ort [lat]/[lng] in Zeitzone [zone].
     * Rückgabe: Liste in der Reihenfolge von [P.ALL]; auf volle Minuten gerundet.
     */
    fun compute(date: LocalDate, lat: Double, lng: Double, zone: ZoneId, p: CalcParams): List<ZonedDateTime> {
        val tzHours = zone.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0
        val jDate = julian(date.year, date.monthValue, date.dayOfMonth) - lng / (15.0 * 24.0)

        fun midDay(time: Double): Double {
            val eqt = sunPosition(jDate + time).eqt
            return fixHour(12 - eqt)
        }
        fun sunAngleTime(angle: Double, time: Double, ccw: Boolean = false): Double {
            val decl = sunPosition(jDate + time).decl
            val noon = midDay(time)
            val t = 1.0 / 15.0 * darccos((-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat)))
            return noon + if (ccw) -t else t
        }
        fun asrTime(factor: Double, time: Double): Double {
            val decl = sunPosition(jDate + time).decl
            val angle = -darccot(factor + dtan(abs(lat - decl)))
            return sunAngleTime(angle, time)
        }

        // Startwerte (Tagesanteile), eine Iteration genügt
        val fajr0 = 5.0 / 24; val rise0 = 6.0 / 24; val dhuhr0 = 12.0 / 24
        val asr0 = 13.0 / 24; val set0 = 18.0 / 24; val isha0 = 18.0 / 24
        val m = p.method

        var fajr = sunAngleTime(m.fajr, fajr0, ccw = true)
        val sunrise = sunAngleTime(0.833, rise0, ccw = true)
        val dhuhr = midDay(dhuhr0)
        val asr = asrTime(if (p.hanafiAsr) 2.0 else 1.0, asr0)
        val sunset = sunAngleTime(0.833, set0)
        val maghrib = sunset
        var isha = if (m.ishaMinutes > 0) Double.NaN else sunAngleTime(m.isha, isha0)

        // In Ortszeit umrechnen
        val shift = tzHours - lng / 15.0
        fajr += shift
        val sunriseL = sunrise + shift
        val dhuhrL = dhuhr + shift
        val asrL = asr + shift
        val sunsetL = sunset + shift
        val maghribL = maghrib + shift
        isha += shift

        // Hohe Breiten
        if (p.highLat != HighLatRule.NONE) {
            val night = timeDiff(sunsetL, sunriseL)
            fun portion(angle: Double): Double = night * when (p.highLat) {
                HighLatRule.ANGLE_BASED -> angle / 60.0
                HighLatRule.ONE_SEVENTH -> 1.0 / 7.0
                else -> 0.5
            }
            val fp = portion(m.fajr)
            if (fajr.isNaN() || timeDiff(fajr, sunriseL) > fp) fajr = sunriseL - fp
            if (m.ishaMinutes == 0) {
                val ip = portion(m.isha)
                if (isha.isNaN() || timeDiff(sunsetL, isha) > ip) isha = sunsetL + ip
            }
        }
        if (m.ishaMinutes > 0) isha = maghribL + m.ishaMinutes / 60.0

        val hours = doubleArrayOf(fajr, sunriseL, dhuhrL, asrL, maghribL, isha)
        val start = date.atStartOfDay() // lokale Wanduhrzeit, DST-sicher
        return P.ALL.map { i ->
            val h = hours[i]
            val totalMin = h * 60.0 + m.adjust[i] + p.userOffsets[i]
            val rounded = if (totalMin.isNaN()) 0L else totalMin.roundToLong()
            start.plusMinutes(rounded).atZone(zone)
        }
    }
}
