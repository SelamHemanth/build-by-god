package com.buildbygod.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.datastore.DietStore
import com.buildbygod.data.repository.IntakeRepository
import com.buildbygod.data.repository.IntakeTotals
import com.buildbygod.data.repository.LoggedFood
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.domain.model.DietPlan
import com.buildbygod.domain.model.DietPlanner
import com.buildbygod.domain.model.FoodCatalog
import com.buildbygod.domain.model.FoodCategory
import com.buildbygod.domain.model.FoodItem
import com.buildbygod.domain.model.NutritionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DietTargets(
    val kcal: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val complete: Boolean
)

/** Ordering options for the food search list. */
enum class FoodSort(val label: String) {
    NAME_AZ("Name A–Z"),
    KCAL_HIGH("Most calories"),
    KCAL_LOW("Fewest calories"),
    PROTEIN_HIGH("Most protein")
}

private data class FoodSearchSpec(
    val query: String,
    val category: FoodCategory?,
    val sort: FoodSort
)

data class DietUiState(
    val targets: DietTargets = DietTargets(2000, 120, 220, 60, false),
    val vegOnly: Boolean = false,
    val pantry: Set<String> = emptySet(),
    val suggested: DietPlan = DietPlan(emptyList(), 0, 0),
    val custom: DietPlan = DietPlan(emptyList(), 0, 0),
    val searchResults: List<FoodItem> = emptyList(),
    val foodCategory: FoodCategory? = null,
    val foodSort: FoodSort = FoodSort.NAME_AZ,
    val followedToday: Boolean = false,
    /** Foods/supplements the user actually logged today. */
    val eaten: List<LoggedFood> = emptyList(),
    val consumed: IntakeTotals = IntakeTotals()
)

@HiltViewModel
class DietViewModel @Inject constructor(
    profileRepo: ProfileRepository,
    private val dietStore: DietStore,
    private val intakeRepo: IntakeRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val foodCategory = MutableStateFlow<FoodCategory?>(null)
    private val foodSort = MutableStateFlow(FoodSort.NAME_AZ)
    private val today get() = LocalDate.now().toEpochDay()

    private val searchSpec = combine(query, foodCategory, foodSort) { q, cat, sort ->
        FoodSearchSpec(q, cat, sort)
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun setFoodCategory(c: FoodCategory?) {
        foodCategory.value = if (foodCategory.value == c) null else c
    }

    fun setFoodSort(s: FoodSort) {
        foodSort.value = s
    }

    fun setVegOnly(value: Boolean) = viewModelScope.launch { dietStore.setVegOnly(value) }
    fun togglePantry(id: String) = viewModelScope.launch { dietStore.togglePantry(id) }
    fun clearPantry() = viewModelScope.launch { dietStore.clearPantry() }
    fun toggleFollowedToday() = viewModelScope.launch { dietStore.toggleFollowedToday() }

    fun logFood(foodId: String, grams: Int) = viewModelScope.launch {
        intakeRepo.log(today, foodId, grams.coerceAtLeast(1))
    }

    fun removeLogged(id: Long) = viewModelScope.launch { intakeRepo.remove(id) }

    val state = combine(
        profileRepo.profile,
        dietStore.prefs,
        searchSpec,
        intakeRepo.observeForDay(today)
    ) { profile, prefs, spec, eaten ->
        val complete = NutritionCalculator.isComplete(profile.weightKg, profile.heightCm, profile.age)
        val targets = if (complete) {
            val plan = NutritionCalculator.compute(
                profile.sex, profile.weightKg, profile.heightCm, profile.age,
                profile.activityLevel, profile.primaryGoal
            )
            DietTargets(plan.calorieTarget, plan.proteinG, plan.carbsG, plan.fatG, true)
        } else {
            DietTargets(2000, 120, 220, 60, false)
        }

        val seed = LocalDate.now().dayOfYear
        val suggested = DietPlanner.generate(
            targetKcal = targets.kcal,
            targetProtein = targets.protein,
            vegOnly = prefs.vegOnly,
            seed = seed
        )
        val custom = if (prefs.pantry.isEmpty()) DietPlan(emptyList(), targets.kcal, targets.protein)
        else DietPlanner.generate(
            targetKcal = targets.kcal,
            targetProtein = targets.protein,
            vegOnly = prefs.vegOnly,
            allowedIds = prefs.pantry,
            seed = seed
        )

        val results = FoodCatalog.search(spec.query, prefs.vegOnly)
            .filter { spec.category == null || it.category == spec.category }
            .let { list ->
                when (spec.sort) {
                    FoodSort.NAME_AZ -> list.sortedBy { it.name.lowercase() }
                    FoodSort.KCAL_HIGH -> list.sortedByDescending { it.kcal }
                    FoodSort.KCAL_LOW -> list.sortedBy { it.kcal }
                    FoodSort.PROTEIN_HIGH -> list.sortedByDescending { it.protein }
                }
            }

        DietUiState(
            targets = targets,
            vegOnly = prefs.vegOnly,
            pantry = prefs.pantry,
            suggested = suggested,
            custom = custom,
            searchResults = results,
            foodCategory = spec.category,
            foodSort = spec.sort,
            followedToday = LocalDate.now().toEpochDay() in prefs.followedDays,
            eaten = eaten,
            consumed = IntakeRepository.totals(eaten)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DietUiState())
}
