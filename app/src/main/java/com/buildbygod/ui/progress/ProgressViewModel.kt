package com.buildbygod.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.entity.SessionLogEntity
import com.buildbygod.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class ProgressUiState(
    val sessions: List<SessionLogEntity> = emptyList(),
    val activeDays: Set<Long> = emptySet(),
    val streak: Int = 0,
    val thisWeek: Int = 0,
    val total: Int = 0
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    repo: ProgressRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    val state = combine(repo.sessions(), repo.sessionDays()) { sessions, days ->
        val set = days.toSet()
        val weekStart = today - 6
        ProgressUiState(
            sessions = sessions,
            activeDays = set,
            streak = computeStreak(set),
            thisWeek = set.count { it in weekStart..today },
            total = sessions.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    private fun computeStreak(days: Set<Long>): Int {
        var streak = 0
        var cursor = today
        if (!days.contains(cursor)) cursor -= 1
        while (days.contains(cursor)) {
            streak += 1
            cursor -= 1
        }
        return streak
    }
}
