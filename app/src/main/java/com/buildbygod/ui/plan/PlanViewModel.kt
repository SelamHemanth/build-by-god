package com.buildbygod.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repo: WorkoutRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val days = repo.days()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun canScheduleExact() = scheduler.canScheduleExact()

    fun setReminder(day: WorkoutDayEntity, enabled: Boolean) =
        save(day.copy(reminderEnabled = enabled))

    fun setTime(day: WorkoutDayEntity, minutes: Int) =
        save(day.copy(scheduledMinutes = minutes))

    fun setRest(day: WorkoutDayEntity, rest: Boolean) =
        save(day.copy(isRestDay = rest, reminderEnabled = if (rest) false else day.reminderEnabled))

    fun rename(day: WorkoutDayEntity, title: String) =
        save(day.copy(title = title))

    private fun save(day: WorkoutDayEntity) {
        viewModelScope.launch {
            repo.saveDay(day)
            scheduler.schedule(day)
        }
    }
}
