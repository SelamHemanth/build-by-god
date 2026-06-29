package com.buildbygod.domain.model

import kotlin.math.roundToInt

enum class MealSlot(val label: String, val share: Double) {
    BREAKFAST("Breakfast", 0.28),
    LUNCH("Lunch", 0.34),
    SNACK("Snack", 0.13),
    DINNER("Dinner", 0.25)
}

data class Meal(val slot: MealSlot, val portions: List<FoodPortion>) {
    val kcal: Int get() = portions.sumOf { it.kcal }
    val protein: Int get() = portions.sumOf { it.protein }
    val carbs: Int get() = portions.sumOf { it.carbs }
    val fat: Int get() = portions.sumOf { it.fat }
}

data class DietPlan(
    val meals: List<Meal>,
    val targetKcal: Int,
    val targetProtein: Int
) {
    val kcal: Int get() = meals.sumOf { it.kcal }
    val protein: Int get() = meals.sumOf { it.protein }
    val carbs: Int get() = meals.sumOf { it.carbs }
    val fat: Int get() = meals.sumOf { it.fat }
    val isEmpty: Boolean get() = meals.all { it.portions.isEmpty() }
}

/**
 * Builds a full day of meals (breakfast → dinner) that roughly hits the calorie and protein targets,
 * honouring a veg-only preference and, optionally, only a set of ingredients the user actually has.
 */
object DietPlanner {

    private val roleWeights = mapOf(
        FoodRole.PROTEIN to 0.35,
        FoodRole.CARB to 0.40,
        FoodRole.VEGFRUIT to 0.10,
        FoodRole.FAT to 0.15
    )

    private val mealTemplates = mapOf(
        MealSlot.BREAKFAST to listOf(FoodRole.PROTEIN, FoodRole.CARB, FoodRole.VEGFRUIT),
        MealSlot.LUNCH to listOf(FoodRole.PROTEIN, FoodRole.CARB, FoodRole.VEGFRUIT, FoodRole.FAT),
        MealSlot.SNACK to listOf(FoodRole.PROTEIN, FoodRole.FAT),
        MealSlot.DINNER to listOf(FoodRole.PROTEIN, FoodRole.CARB, FoodRole.VEGFRUIT)
    )

    fun generate(
        targetKcal: Int,
        targetProtein: Int,
        vegOnly: Boolean,
        allowedIds: Set<String>? = null,
        seed: Int = 0
    ): DietPlan {
        val kcal = targetKcal.coerceAtLeast(1200)
        val pool = FoodCatalog.items.filter {
            it.kcal > 0 && (!vegOnly || it.veg) && (allowedIds == null || it.id in allowedIds)
        }
        val byRole = FoodRole.entries.associateWith { role -> pool.filter { it.role == role } }

        val meals = MealSlot.entries.mapIndexed { mealIdx, slot ->
            val mealKcal = kcal * slot.share
            val roles = mealTemplates.getValue(slot).filter { byRole[it]?.isNotEmpty() == true }
            if (roles.isEmpty()) return@mapIndexed Meal(slot, emptyList())

            val totalWeight = roles.sumOf { roleWeights[it] ?: 0.2 }
            val portions = roles.mapNotNull { role ->
                val options = byRole[role].orEmpty()
                if (options.isEmpty()) return@mapNotNull null
                val food = options[(seed + mealIdx * 3 + role.ordinal) % options.size]
                val roleKcal = mealKcal * (roleWeights[role] ?: 0.2) / totalWeight
                val perGram = food.kcal / 100.0
                if (perGram <= 0.0) return@mapNotNull null
                val grams = (roleKcal / perGram / 5.0).roundToInt() * 5
                FoodPortion(food, grams.coerceIn(10, 400))
            }
            Meal(slot, portions)
        }
        return DietPlan(meals, kcal, targetProtein)
    }
}
