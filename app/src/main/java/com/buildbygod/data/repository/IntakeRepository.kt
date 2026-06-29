package com.buildbygod.data.repository

import com.buildbygod.data.local.dao.IntakeDao
import com.buildbygod.data.local.entity.IntakeEntity
import com.buildbygod.domain.model.FoodCatalog
import com.buildbygod.domain.model.FoodItem
import com.buildbygod.domain.model.FoodPortion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** A logged intake row resolved against the catalog so its macros are available. */
data class LoggedFood(
    val id: Long,
    val portion: FoodPortion
) {
    val food: FoodItem get() = portion.food
}

/** Running macro totals for a set of logged foods. */
data class IntakeTotals(
    val kcal: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0
)

@Singleton
class IntakeRepository @Inject constructor(
    private val dao: IntakeDao
) {
    fun observeForDay(epochDay: Long): Flow<List<LoggedFood>> =
        dao.observeForDay(epochDay).map { rows -> rows.mapNotNull { it.toLogged() } }

    suspend fun log(epochDay: Long, foodId: String, grams: Int) {
        dao.insert(IntakeEntity(epochDay = epochDay, foodId = foodId, grams = grams))
    }

    suspend fun remove(id: Long) = dao.delete(id)

    private fun IntakeEntity.toLogged(): LoggedFood? {
        val food = FoodCatalog.byId(foodId) ?: return null
        return LoggedFood(id, FoodPortion(food, grams))
    }

    companion object {
        fun totals(items: List<LoggedFood>): IntakeTotals = IntakeTotals(
            kcal = items.sumOf { it.portion.kcal },
            protein = items.sumOf { it.portion.protein },
            carbs = items.sumOf { it.portion.carbs },
            fat = items.sumOf { it.portion.fat }
        )
    }
}
