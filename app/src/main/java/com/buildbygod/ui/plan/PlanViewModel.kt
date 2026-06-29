package com.buildbygod.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.DayPlanner
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repo: WorkoutRepository,
    private val profileRepo: ProfileRepository,
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

    /** Preview which muscle groups a typed day name implies (for the auto-suggest dialog). */
    fun previewGroups(name: String): List<String> =
        DayPlanner.parseGroups(name).map { it.label }

    /**
     * Renames the day and auto-generates warm-ups / main exercises / stretches from the name,
     * tailored to the user's experience and goal.
     */
    fun autoFill(day: WorkoutDayEntity, name: String) {
        viewModelScope.launch {
            val profile = profileRepo.profile.first()
            repo.autoFillDayFromName(day.dayOfWeek, name, profile.experience, profile.primaryGoal)
            scheduler.schedule(repo.day(day.dayOfWeek).first() ?: day.copy(title = name))
        }
    }

    private fun save(day: WorkoutDayEntity) {
        viewModelScope.launch {
            repo.saveDay(day)
            scheduler.schedule(day)
        }
    }
}
