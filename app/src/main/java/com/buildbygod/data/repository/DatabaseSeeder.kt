package com.buildbygod.data.repository

import com.buildbygod.data.local.SeedData
import com.buildbygod.data.local.dao.ExerciseDao
import com.buildbygod.data.local.dao.WorkoutDao
import javax.inject.Inject
import javax.inject.Singleton

/** Populates the curated library + default weekly plan on first launch. */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao
) {
    suspend fun seedIfNeeded() {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(SeedData.exercises)
        }
        if (workoutDao.dayCount() == 0) {
            workoutDao.upsertDays(SeedData.defaultDays)
            workoutDao.insertDayExercises(SeedData.defaultPlan())
        }
    }
}
