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

/** Body composition snapshot derived from the user's profile and current weight. */
data class BodyComposition(
    val weightKg: Float,
    val startWeightKg: Float,
    val targetWeightKg: Float,
    val goal: Goal,
    val bmi: Float,
    val bmiCategory: String,
    val bodyFatPct: Float,
    val bodyFatCategory: String,
    val leanMassKg: Float,
    val fatMassKg: Float,
    val healthyLowKg: Float,
    val healthyHighKg: Float,
    /** Signed kg still to change to reach target: positive = gain, negative = lose. */
    val toTargetKg: Float,
    /** Fraction of the start -> target journey already covered (0..1). */
    val targetProgress: Float
)

/**
 * Estimates BMI, body fat (Deurenberg), lean/fat mass and a goal-aware target weight
 * from the same body details the profile already collects.
 */
object BodyMetrics {

    fun isComplete(weightKg: Float, heightCm: Int, age: Int): Boolean =
        weightKg > 0f && heightCm > 0 && age > 0

    fun compute(
        sex: Sex,
        weightKg: Float,
        heightCm: Int,
        age: Int,
        goal: Goal,
        startWeightKg: Float
    ): BodyComposition {
        val hM = heightCm / 100.0
        val bmi = (weightKg / (hM * hM)).toFloat()

        val healthyLow = weightAtBmi(18.5, hM)
        val healthyHigh = weightAtBmi(24.9, hM)
        val ideal = weightAtBmi(22.0, hM)

        val target = when (goal) {
            Goal.LOSE_FAT -> minOf(weightKg, ideal)
            Goal.BUILD_MUSCLE, Goal.GET_STRONGER ->
                maxOf(weightKg + 2f, ideal).coerceAtMost(healthyHigh + 4f)
            else -> if (weightKg in healthyLow..healthyHigh) weightKg else ideal
        }

        val sexFactor = if (sex == Sex.MALE) 1 else 0
        val bodyFat = (1.20f * bmi + 0.23f * age - 10.8f * sexFactor - 5.4f).coerceIn(3f, 60f)
        val leanMass = weightKg * (1f - bodyFat / 100f)
        val fatMass = weightKg - leanMass

        val start = if (startWeightKg > 0f) startWeightKg else weightKg
        val progress = if (target == start) 1f
        else ((weightKg - start) / (target - start)).coerceIn(0f, 1f)

        return BodyComposition(
            weightKg = weightKg,
            startWeightKg = start,
            targetWeightKg = target,
            goal = goal,
            bmi = (Math.round(bmi * 10f) / 10f),
            bmiCategory = bmiCategory(bmi),
            bodyFatPct = (Math.round(bodyFat * 10f) / 10f),
            bodyFatCategory = bodyFatCategory(sex, bodyFat),
            leanMassKg = (Math.round(leanMass * 10f) / 10f),
            fatMassKg = (Math.round(fatMass * 10f) / 10f),
            healthyLowKg = (Math.round(healthyLow * 10f) / 10f),
            healthyHighKg = (Math.round(healthyHigh * 10f) / 10f),
            toTargetKg = (Math.round((target - weightKg) * 10f) / 10f),
            targetProgress = progress
        )
    }

    private fun weightAtBmi(bmi: Double, hM: Double): Float = (bmi * hM * hM).toFloat()

    private fun bmiCategory(bmi: Float): String = when {
        bmi < 18.5f -> "Underweight"
        bmi < 25f -> "Healthy"
        bmi < 30f -> "Overweight"
        else -> "Obese"
    }

    private fun bodyFatCategory(sex: Sex, bf: Float): String =
        if (sex == Sex.MALE) when {
            bf < 6f -> "Essential"
            bf < 14f -> "Athletic"
            bf < 18f -> "Fitness"
            bf < 25f -> "Average"
            else -> "High"
        } else when {
            bf < 14f -> "Essential"
            bf < 21f -> "Athletic"
            bf < 25f -> "Fitness"
            bf < 32f -> "Average"
            else -> "High"
        }
}
