package com.buildbygod.domain.model

import kotlin.math.roundToInt

data class NutritionPlan(
    val bmr: Int,
    val tdee: Int,
    val calorieTarget: Int,
    val proteinG: Int,
    val fatG: Int,
    val carbsG: Int,
    val waterMl: Int
)

/**
 * Computes daily energy + macro targets using the Mifflin-St Jeor equation,
 * adjusted by activity level and training goal.
 */
object NutritionCalculator {

    fun isComplete(weightKg: Float, heightCm: Int, age: Int): Boolean =
        weightKg > 0f && heightCm > 0 && age > 0

    fun compute(
        sex: Sex,
        weightKg: Float,
        heightCm: Int,
        age: Int,
        activity: ActivityLevel,
        goal: Goal
    ): NutritionPlan {
        val bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + (if (sex == Sex.MALE) 5 else -161)
        val tdee = bmr * activity.factor
        val calorieTarget = when (goal) {
            Goal.LOSE_FAT -> tdee - 500
            Goal.BUILD_MUSCLE -> tdee + 300
            Goal.GET_STRONGER -> tdee + 200
            else -> tdee
        }
        val proteinPerKg = when (goal) {
            Goal.LOSE_FAT -> 2.2
            Goal.BUILD_MUSCLE, Goal.GET_STRONGER -> 2.0
            else -> 1.6
        }
        val proteinG = (proteinPerKg * weightKg).roundToInt()
        val fatKcal = calorieTarget * 0.25
        val fatG = (fatKcal / 9.0).roundToInt()
        val remainingKcal = calorieTarget - (proteinG * 4) - (fatG * 9)
        val carbsG = (remainingKcal / 4.0).coerceAtLeast(0.0).roundToInt()
        val waterMl = (weightKg * 35).roundToInt()

        return NutritionPlan(
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            calorieTarget = calorieTarget.roundToInt(),
            proteinG = proteinG,
            fatG = fatG,
            carbsG = carbsG,
            waterMl = waterMl
        )
    }

    /** Rough calories burned for a logged session via MET estimate (~6 MET strength training). */
    fun caloriesBurned(weightKg: Float, durationSeconds: Long, met: Double = 6.0): Int {
        if (weightKg <= 0f) return 0
        val hours = durationSeconds / 3600.0
        return (met * weightKg * hours).roundToInt()
    }
}
