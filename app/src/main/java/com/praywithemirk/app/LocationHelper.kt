package com.praywithemirk.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import java.util.Locale

/** Standortbestimmung ohne Google-Play-Dienste (nur Android-Framework). */
object LocationHelper {

    fun hasPermission(ctx: Context): Boolean =
        ctx.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Liefert einen Standort (oder null) über [cb] im Haupt-Thread. */
    @SuppressLint("MissingPermission")
    fun locate(ctx: Context, cb: (Location?) -> Unit) {
        if (!hasPermission(ctx)) { cb(null); return }
        val lm = ctx.getSystemService(LocationManager::class.java)
        val main = Handler(Looper.getMainLooper())

        // 1) Frischer letzter bekannter Standort (< 30 min)
        val fresh = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { p -> try { lm.getLastKnownLocation(p) } catch (_: Exception) { null } }
            .maxByOrNull { it.time }
        if (fresh != null && System.currentTimeMillis() - fresh.time < 30 * 60_000L) {
            cb(fresh); return
        }

        val provider = when {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> null
        }
        if (provider == null) { cb(fresh); return }

        var done = false
        fun deliver(l: Location?) {
            if (done) return
            done = true
            cb(l ?: fresh)
        }

        // 2) Aktuellen Standort anfordern, mit Zeitlimit
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lm.removeUpdates(this)
                deliver(location)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                lm.getCurrentLocation(provider, null, ctx.mainExecutor) { l -> deliver(l) }
            } else {
                lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
        } catch (e: Exception) {
            deliver(null)
        }
        main.postDelayed({
            try { lm.removeUpdates(listener) } catch (_: Exception) {}
            deliver(null)
        }, 20_000L)
    }

    /** Ortsname zu Koordinaten (Hintergrund-Thread, Rückgabe im Haupt-Thread). */
    fun placeName(ctx: Context, lat: Double, lng: Double, cb: (String?) -> Unit) {
        val main = Handler(Looper.getMainLooper())
        Thread {
            val name = try {
                @Suppress("DEPRECATION")
                val a = Geocoder(ctx, Locale.getDefault()).getFromLocation(lat, lng, 1)?.firstOrNull()
                a?.let { label(it) }
            } catch (e: Exception) { null }
            main.post { cb(name) }
        }.start()
    }

    /** Stadtsuche über den Geocoder des Geräts. */
    fun search(ctx: Context, query: String, cb: (List<Address>) -> Unit) {
        val main = Handler(Looper.getMainLooper())
        Thread {
            val list = try {
                @Suppress("DEPRECATION")
                Geocoder(ctx, Locale.getDefault()).getFromLocationName(query, 5) ?: emptyList()
            } catch (e: Exception) { emptyList() }
            main.post { cb(list) }
        }.start()
    }

    fun label(a: Address): String {
        val city = a.locality ?: a.subAdminArea ?: a.adminArea ?: a.featureName ?: ""
        val country = a.countryName ?: ""
        return listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")
    }
}
