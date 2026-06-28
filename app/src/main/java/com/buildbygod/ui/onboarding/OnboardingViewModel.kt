package com.buildbygod.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.HeightUnit
import com.buildbygod.domain.model.Sex
import com.buildbygod.domain.model.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val workoutRepo: WorkoutRepository
) : ViewModel() {

    fun finish(
        name: String,
        dobEpochDay: Long,
        sex: Sex,
        heightCm: Int,
        heightUnit: HeightUnit,
        weightKg: Float,
        weightUnit: WeightUnit,
        experience: ExperienceLevel,
        goals: Set<Goal>,
        applySuggestedPlan: Boolean
    ) {
        viewModelScope.launch {
            val finalGoals = goals.ifEmpty { setOf(Goal.STAY_FIT) }
            if (applySuggestedPlan) {
                workoutRepo.applyPersonalPlan(finalGoals, experience)
            }
            repo.update {
                it.copy(
                    onboarded = true,
                    name = name.ifBlank { "Athlete" },
                    dobEpochDay = dobEpochDay,
                    sex = sex,
                    heightCm = heightCm,
                    heightUnit = heightUnit,
                    weightKg = weightKg,
                    startWeight = if (weightKg > 0f) weightKg else it.startWeight,
                    weightUnit = weightUnit,
                    experience = experience,
                    goals = finalGoals
                )
            }
        }
    }
}
