package com.buildbygod.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    fun finish(name: String, goal: Goal, units: Units) {
        viewModelScope.launch {
            repo.update {
                it.copy(
                    onboarded = true,
                    name = name.ifBlank { "Athlete" },
                    goal = goal,
                    units = units
                )
            }
        }
    }
}
