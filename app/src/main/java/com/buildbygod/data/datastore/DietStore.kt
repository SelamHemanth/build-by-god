package com.buildbygod.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

val Context.dietDataStore by preferencesDataStore(name = "diet")

data class DietPrefs(
    val vegOnly: Boolean = false,
    /** Ingredient ids the user marked as available, for custom plans. */
    val pantry: Set<String> = emptySet(),
    /** Epoch days the user marked their diet as "on track", for adherence stats. */
    val followedDays: Set<Long> = emptySet()
)

@Singleton
class DietStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val VEG = booleanPreferencesKey("veg_only")
        val PANTRY = stringPreferencesKey("pantry")
        val FOLLOWED = stringPreferencesKey("followed_days")
    }

    val prefs: Flow<DietPrefs> = context.dietDataStore.data.map { p ->
        DietPrefs(
            vegOnly = p[Keys.VEG] ?: false,
            pantry = (p[Keys.PANTRY] ?: "").split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
            followedDays = (p[Keys.FOLLOWED] ?: "").split(',').mapNotNull { it.trim().toLongOrNull() }.toSet()
        )
    }

    /** Toggle whether today's diet was followed (for adherence tracking). */
    suspend fun toggleFollowedToday() {
        val today = LocalDate.now().toEpochDay()
        context.dietDataStore.edit { p ->
            val current = (p[Keys.FOLLOWED] ?: "").split(',').mapNotNull { it.trim().toLongOrNull() }.toMutableSet()
            if (!current.add(today)) current.remove(today)
            p[Keys.FOLLOWED] = current.joinToString(",")
        }
    }

    suspend fun setVegOnly(value: Boolean) {
        context.dietDataStore.edit { it[Keys.VEG] = value }
    }

    suspend fun togglePantry(id: String) {
        context.dietDataStore.edit { p ->
            val current = (p[Keys.PANTRY] ?: "").split(',').map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
            if (!current.add(id)) current.remove(id)
            p[Keys.PANTRY] = current.joinToString(",")
        }
    }

    suspend fun clearPantry() {
        context.dietDataStore.edit { it[Keys.PANTRY] = "" }
    }
}
