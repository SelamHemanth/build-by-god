package com.buildbygod.data.repository

import android.content.Context
import com.buildbygod.data.local.DayPlanner
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

    /**
     * Renames a day and regenerates its warm-up / main / stretch exercises from the implied muscle
     * groups, tailored to the user's experience and goal. Used by the weekly-plan auto-suggest.
     */
    suspend fun autoFillDayFromName(
        day: Int,
        name: String,
        experience: ExperienceLevel,
        goal: Goal
    ) {
        val exercises = exerciseDao.getAll()
        if (exercises.isEmpty()) return
        val gen = DayPlanner.build(name, exercises, experience, goal)
        val existing = dao.getDay(day)
        val updatedDay = (existing ?: WorkoutDayEntity(day, gen.title, gen.focus))
            .copy(title = gen.title, focus = gen.focus, isRestDay = false)
        dao.upsertDay(updatedDay)
        dao.clearDay(day)
        dao.insertDayExercises(gen.refs.map { it.copy(dayOfWeek = day) })
        TodayWidgetProvider.refresh(context)
    }

    suspend fun addExerciseToDay(
        day: Int,
        exerciseId: String,
        section: String,
        sets: Int,
        reps: String,
        durationSeconds: Int = -1
    ) {
        val nextOrder = (dao.maxOrder(day, section) ?: -1) + 1
        dao.insertDayExercise(
            DayExerciseCrossRef(
                dayOfWeek = day,
                exerciseId = exerciseId,
                section = section,
                orderIndex = nextOrder,
                sets = sets,
                reps = reps,
                durationSeconds = durationSeconds
            )
        )
        TodayWidgetProvider.refresh(context)
    }

    suspend fun updateDayExercise(ref: DayExerciseCrossRef) = dao.updateDayExercise(ref)

    /** Update just the sets/reps/duration prescription for one day-exercise. */
    suspend fun updatePrescription(dxId: Long, sets: Int, reps: String, durationSeconds: Int) {
        dao.updatePrescription(dxId, sets, reps, durationSeconds)
        TodayWidgetProvider.refresh(context)
    }
    suspend fun removeDayExercise(id: Long) {
        dao.deleteDayExercise(id)
        TodayWidgetProvider.refresh(context)
    }
}
