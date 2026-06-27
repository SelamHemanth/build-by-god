package com.buildbygod.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.repository.DatabaseSeeder
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppState {
    data object Loading : AppState
    data object Onboarding : AppState
    data object Ready : AppState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val seeder: DatabaseSeeder,
    private val profileRepo: ProfileRepository,
    private val workoutRepo: WorkoutRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    val appState = profileRepo.profile
        .map { if (it.onboarded) AppState.Ready else AppState.Onboarding }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState.Loading)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            seeder.seedIfNeeded()
            scheduler.rescheduleAll(workoutRepo.reminderDays())
        }
    }
}
