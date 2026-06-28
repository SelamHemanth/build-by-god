package com.buildbygod.data.repository

import android.content.Context
import com.buildbygod.data.local.PlanGenerator
import com.buildbygod.data.local.dao.DayExerciseWithInfo
import com.buildbygod.data.local.dao.ExerciseDao
import com.buildbygod.data.local.dao.WorkoutDao
import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.widget.TodayWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: WorkoutDao,
    private val exerciseDao: ExerciseDao
) {
    fun days(): Flow<List<WorkoutDayEntity>> = dao.observeDays()
    fun day(day: Int): Flow<WorkoutDayEntity?> = dao.observeDay(day)
    fun dayExercises(day: Int): Flow<List<DayExerciseWithInfo>> = dao.observeDayExercises(day)

    suspend fun saveDay(day: WorkoutDayEntity) {
        dao.upsertDay(day)
        TodayWidgetProvider.refresh(context)
    }
    suspend fun reminderDays() = dao.daysWithReminders()

    /** Replaces the whole weekly plan with one generated from the user's goals + experience. */
    suspend fun applyPersonalPlan(goals: Set<Goal>, experience: ExperienceLevel) {
        val exercises = exerciseDao.getAll()
        if (exercises.isEmpty()) return
        val plan = PlanGenerator.build(goals, experience)
        val (days, refs) = PlanGenerator.buildRows(plan, exercises, goals)
        dao.clearDayExercises()
        dao.upsertDays(days)
        dao.insertDayExercises(refs)
        TodayWidgetProvider.refresh(context)
    }

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
        TodayWidgetProvider.refresh(context)
    }

    suspend fun updateDayExercise(ref: DayExerciseCrossRef) = dao.updateDayExercise(ref)
    suspend fun removeDayExercise(id: Long) {
        dao.deleteDayExercise(id)
        TodayWidgetProvider.refresh(context)
    }
}
