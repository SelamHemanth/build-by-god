package com.buildbygod.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.waterDataStore by preferencesDataStore(name = "water_reminder")

/**
 * User preferences for the water-intake reminder.
 * Times are stored as minutes-since-midnight so they survive locale/timezone changes.
 */
data class WaterReminderPrefs(
    val enabled: Boolean = false,
    val intervalMinutes: Int = 90,
    val startMinutes: Int = 8 * 60,
    val endMinutes: Int = 22 * 60
)

@Singleton
class WaterReminderStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("water_enabled")
        val INTERVAL = intPreferencesKey("water_interval")
        val START = intPreferencesKey("water_start")
        val END = intPreferencesKey("water_end")
    }

    val prefs: Flow<WaterReminderPrefs> = context.waterDataStore.data.map { p ->
        WaterReminderPrefs(
            enabled = p[Keys.ENABLED] ?: false,
            intervalMinutes = (p[Keys.INTERVAL] ?: 90).coerceIn(15, 360),
            startMinutes = (p[Keys.START] ?: 8 * 60).coerceIn(0, 24 * 60 - 1),
            endMinutes = (p[Keys.END] ?: 22 * 60).coerceIn(0, 24 * 60 - 1)
        )
    }

    suspend fun setEnabled(value: Boolean) =
        context.waterDataStore.edit { it[Keys.ENABLED] = value }

    suspend fun setInterval(minutes: Int) =
        context.waterDataStore.edit { it[Keys.INTERVAL] = minutes.coerceIn(15, 360) }

    suspend fun setWindow(startMinutes: Int, endMinutes: Int) =
        context.waterDataStore.edit {
            it[Keys.START] = startMinutes.coerceIn(0, 24 * 60 - 1)
            it[Keys.END] = endMinutes.coerceIn(0, 24 * 60 - 1)
        }
}
