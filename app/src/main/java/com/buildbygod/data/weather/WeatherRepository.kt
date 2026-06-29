package com.buildbygod.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.buildbygod.domain.model.WeatherCondition
import com.buildbygod.domain.model.WeatherInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches current local weather from the free, keyless Open-Meteo API using the device's last known
 * coarse location. Everything degrades gracefully: with no permission, location or network we still
 * return a sensible season-based [WeatherInfo] so the background always has something to show.
 */
@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile
    private var cache: WeatherInfo? = null

    suspend fun current(): WeatherInfo {
        cache?.let { return it }
        val info = withContext(Dispatchers.IO) { fetch() }
        // Only cache real results, so a later retry (e.g. after location permission is granted) works.
        if (info.resolved) cache = info
        return info
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun lastKnownLatLon(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return runCatching {
            val providers = lm.getProviders(true)
            var best: android.location.Location? = null
            for (p in providers) {
                val loc = lm.getLastKnownLocation(p) ?: continue
                if (best == null || loc.accuracy < best!!.accuracy) best = loc
            }
            best?.let { it.latitude to it.longitude }
        }.getOrNull()
    }

    private fun fetch(): WeatherInfo {
        val today = LocalDate.now()
        val latLon = lastKnownLatLon()
        val seasonFallback = WeatherInfo.seasonFor(today, latLon?.first)

        if (latLon == null) {
            return WeatherInfo(
                condition = WeatherCondition.CLEAR,
                season = seasonFallback,
                resolved = false
            )
        }

        val (lat, lon) = latLon
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,weather_code,is_day"
        val json = runCatching {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        }.getOrNull()

        if (json == null) {
            return WeatherInfo(season = seasonFallback, resolved = false)
        }

        return runCatching {
            val current = JSONObject(json).getJSONObject("current")
            val code = current.optInt("weather_code", 0)
            val temp = current.optDouble("temperature_2m", Double.NaN)
            val isDay = current.optInt("is_day", 1) == 1
            WeatherInfo(
                condition = WeatherInfo.conditionForWmo(code),
                season = WeatherInfo.seasonFor(today, lat),
                temperatureC = if (temp.isNaN()) null else temp.toFloat(),
                isDay = isDay,
                resolved = true
            )
        }.getOrElse {
            WeatherInfo(season = seasonFallback, resolved = false)
        }
    }
}
