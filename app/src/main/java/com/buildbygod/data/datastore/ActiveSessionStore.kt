package com.buildbygod.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.activeSessionDataStore by preferencesDataStore(name = "active_session")

/**
 * A workout the user has started but not finished. Persisted so the session survives the app being
 * backgrounded or killed, and so Home can offer a "Resume" card.
 *
 * [elapsedBeforePause] accumulates seconds already trained; while [paused] is false the live elapsed
 * time is [elapsedBeforePause] + (now - [resumedAt]).
 */
data class ActiveSession(
    val active: Boolean = false,
    val day: Int = 1,
    val title: String = "",
    val index: Int = 0,
    val total: Int = 0,
    val completed: Int = 0,
    /** Comma-separated dxIds already marked done, so the exact set can be restored on resume. */
    val completedIds: String = "",
    val paused: Boolean = false,
    val resumedAt: Long = 0L,
    val elapsedBeforePause: Long = 0L
) {
    fun completedIdSet(): Set<Long> =
        completedIds.split(',').mapNotNull { it.trim().toLongOrNull() }.toSet()

    /** Total seconds trained so far, accounting for the running segment. */
    fun elapsedSeconds(now: Long = System.currentTimeMillis()): Long =
        if (paused) elapsedBeforePause else elapsedBeforePause + (now - resumedAt) / 1000
}

@Singleton
class ActiveSessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACTIVE = booleanPreferencesKey("active")
        val DAY = intPreferencesKey("day")
        val TITLE = stringPreferencesKey("title")
        val INDEX = intPreferencesKey("index")
        val TOTAL = intPreferencesKey("total")
        val COMPLETED = intPreferencesKey("completed")
        val COMPLETED_IDS = stringPreferencesKey("completed_ids")
        val PAUSED = booleanPreferencesKey("paused")
        val RESUMED_AT = longPreferencesKey("resumed_at")
        val ELAPSED = longPreferencesKey("elapsed_before_pause")
    }

    val session: Flow<ActiveSession> = context.activeSessionDataStore.data.map { p ->
        ActiveSession(
            active = p[Keys.ACTIVE] ?: false,
            day = p[Keys.DAY] ?: 1,
            title = p[Keys.TITLE] ?: "",
            index = p[Keys.INDEX] ?: 0,
            total = p[Keys.TOTAL] ?: 0,
            completed = p[Keys.COMPLETED] ?: 0,
            completedIds = p[Keys.COMPLETED_IDS] ?: "",
            paused = p[Keys.PAUSED] ?: false,
            resumedAt = p[Keys.RESUMED_AT] ?: 0L,
            elapsedBeforePause = p[Keys.ELAPSED] ?: 0L
        )
    }

    /** Begin (or replace) the active session, running from now. */
    suspend fun begin(day: Int, title: String, total: Int) {
        context.activeSessionDataStore.edit { p ->
            p[Keys.ACTIVE] = true
            p[Keys.DAY] = day
            p[Keys.TITLE] = title
            p[Keys.INDEX] = 0
            p[Keys.TOTAL] = total
            p[Keys.COMPLETED] = 0
            p[Keys.COMPLETED_IDS] = ""
            p[Keys.PAUSED] = false
            p[Keys.RESUMED_AT] = System.currentTimeMillis()
            p[Keys.ELAPSED] = 0L
        }
    }

    /** Update progress (current exercise index and the set of completed dxIds). */
    suspend fun updateProgress(index: Int, completedIds: Set<Long>) {
        context.activeSessionDataStore.edit { p ->
            if (p[Keys.ACTIVE] == true) {
                p[Keys.INDEX] = index
                p[Keys.COMPLETED] = completedIds.size
                p[Keys.COMPLETED_IDS] = completedIds.joinToString(",")
            }
        }
    }

    suspend fun setPaused(paused: Boolean) {
        context.activeSessionDataStore.edit { p ->
            if (p[Keys.ACTIVE] != true) return@edit
            val wasPaused = p[Keys.PAUSED] ?: false
            if (paused == wasPaused) return@edit
            if (paused) {
                val resumedAt = p[Keys.RESUMED_AT] ?: System.currentTimeMillis()
                val prior = p[Keys.ELAPSED] ?: 0L
                p[Keys.ELAPSED] = prior + (System.currentTimeMillis() - resumedAt) / 1000
                p[Keys.PAUSED] = true
            } else {
                p[Keys.RESUMED_AT] = System.currentTimeMillis()
                p[Keys.PAUSED] = false
            }
        }
    }

    suspend fun clear() {
        context.activeSessionDataStore.edit { it.clear() }
    }
}
