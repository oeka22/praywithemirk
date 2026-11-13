package com.praywithemirk.app

import java.time.ZonedDateTime

/** Ein konkreter Gebetszeitpunkt. */
class PrayerEvent(val prayer: Int, val time: ZonedDateTime)

object Schedule {

    /** Alle Zeiten von gestern bis [daysAhead] Tage in die Zukunft, chronologisch. */
    fun events(prefs: Prefs, now: ZonedDateTime, daysAhead: Int = 2): List<PrayerEvent> {
        val today = now.toLocalDate()
        val out = ArrayList<PrayerEvent>()
        for (d in -1..daysAhead) {
            val date = today.plusDays(d.toLong())
            val times = prefs.timesFor(date, now.zone)
            for (i in P.ALL) out.add(PrayerEvent(i, times[i]))
        }
        out.sortBy { it.time.toInstant() }
        return out
    }

    /** Nächster Zeitpunkt (inkl. Sonnenaufgang) nach [now]. */
    fun next(prefs: Prefs, now: ZonedDateTime): PrayerEvent =
        events(prefs, now).first { it.time.isAfter(now) }

    /** Letzter Zeitpunkt (inkl. Sonnenaufgang) vor oder genau zu [now]. */
    fun previous(prefs: Prefs, now: ZonedDateTime): PrayerEvent =
        events(prefs, now).last { !it.time.isAfter(now) }

    /** Aktuelles Gebet für die Adhkar-Seite (Sonnenaufgang zählt zu Fajr). */
    fun currentSalah(prefs: Prefs, now: ZonedDateTime): Int {
        val p = previous(prefs, now).prayer
        return if (p == P.SUNRISE) P.FAJR else p
    }
}
