package com.buildbygod.data.repository

import android.content.Context
import com.buildbygod.data.local.SeedData
import com.buildbygod.data.local.dao.ExerciseDao
import com.buildbygod.data.local.dao.WorkoutDao
import com.buildbygod.data.local.entity.ExerciseEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Populates the full offline library + default weekly plan on first launch. */
@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao
) {
    suspend fun seedIfNeeded() {
        val exercises = SeedData.loadExercises(context)
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(exercises)
        } else {
            reconcileLibrary(exercises)
        }
        if (workoutDao.dayCount() == 0) {
            workoutDao.upsertDays(SeedData.defaultDays)
            workoutDao.insertDayExercises(SeedData.buildPlan(exercises))
        }
    }

    /**
     * Keeps an already-seeded library in sync with the bundled catalogue without wiping user data:
     * removes exercises no longer shipped (e.g. de-duplicated entries) and adds any newly added ones.
     * Favorites and existing rows are preserved (we never blanket-replace).
     */
    private suspend fun reconcileLibrary(exercises: List<ExerciseEntity>) {
        val assetIds = exercises.map { it.id }.toSet()
        val dbIds = exerciseDao.allIds().toSet()

        val orphanIds = (dbIds - assetIds).toList()
        if (orphanIds.isNotEmpty()) {
            workoutDao.deleteDayExercisesByExerciseIds(orphanIds)
            exerciseDao.deleteByIds(orphanIds)
        }

        val missing = exercises.filter { it.id !in dbIds }
        if (missing.isNotEmpty()) {
            exerciseDao.insertAll(missing)
        }
    }
}
