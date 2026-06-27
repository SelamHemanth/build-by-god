package com.buildbygod.data.repository

import com.buildbygod.data.local.dao.DayExerciseWithInfo
import com.buildbygod.data.local.dao.WorkoutDao
import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.WorkoutDayEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val dao: WorkoutDao
) {
    fun days(): Flow<List<WorkoutDayEntity>> = dao.observeDays()
    fun day(day: Int): Flow<WorkoutDayEntity?> = dao.observeDay(day)
    fun dayExercises(day: Int): Flow<List<DayExerciseWithInfo>> = dao.observeDayExercises(day)

    suspend fun saveDay(day: WorkoutDayEntity) = dao.upsertDay(day)
    suspend fun reminderDays() = dao.daysWithReminders()

    suspend fun addExerciseToDay(day: Int, exerciseId: String, section: String, sets: Int, reps: String) {
        val nextOrder = (dao.maxOrder(day, section) ?: -1) + 1
        dao.insertDayExercise(
            DayExerciseCrossRef(
                dayOfWeek = day,
                exerciseId = exerciseId,
                section = section,
                orderIndex = nextOrder,
                sets = sets,
                reps = reps
            )
        )
    }

    suspend fun updateDayExercise(ref: DayExerciseCrossRef) = dao.updateDayExercise(ref)
    suspend fun removeDayExercise(id: Long) = dao.deleteDayExercise(id)
}
