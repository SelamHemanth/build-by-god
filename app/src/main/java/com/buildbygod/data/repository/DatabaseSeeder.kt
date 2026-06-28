package com.buildbygod.data.repository

import android.content.Context
import com.buildbygod.data.local.SeedData
import com.buildbygod.data.local.dao.ExerciseDao
import com.buildbygod.data.local.dao.WorkoutDao
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
        }
        if (workoutDao.dayCount() == 0) {
            workoutDao.upsertDays(SeedData.defaultDays)
            workoutDao.insertDayExercises(SeedData.buildPlan(exercises))
        }
    }
}
