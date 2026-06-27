package com.buildbygod.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.WorkoutDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ---- Days ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: WorkoutDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDays(days: List<WorkoutDayEntity>)

    @Query("SELECT * FROM workout_days ORDER BY dayOfWeek")
    fun observeDays(): Flow<List<WorkoutDayEntity>>

    @Query("SELECT * FROM workout_days WHERE dayOfWeek = :day")
    fun observeDay(day: Int): Flow<WorkoutDayEntity?>

    @Query("SELECT * FROM workout_days WHERE reminderEnabled = 1 AND isRestDay = 0 AND scheduledMinutes >= 0")
    suspend fun daysWithReminders(): List<WorkoutDayEntity>

    @Query("SELECT COUNT(*) FROM workout_days")
    suspend fun dayCount(): Int

    // ---- Day exercises ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayExercise(ref: DayExerciseCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayExercises(refs: List<DayExerciseCrossRef>)

    @Update
    suspend fun updateDayExercise(ref: DayExerciseCrossRef)

    @Query("DELETE FROM day_exercises WHERE id = :id")
    suspend fun deleteDayExercise(id: Long)

    @Query(
        """
        SELECT e.*, dx.id AS dxId, dx.section AS dxSection, dx.orderIndex AS dxOrder,
               dx.sets AS dxSets, dx.reps AS dxReps
        FROM day_exercises dx
        INNER JOIN exercises e ON e.id = dx.exerciseId
        WHERE dx.dayOfWeek = :day
        ORDER BY dx.orderIndex
        """
    )
    fun observeDayExercises(day: Int): Flow<List<DayExerciseWithInfo>>

    @Query("SELECT MAX(orderIndex) FROM day_exercises WHERE dayOfWeek = :day AND section = :section")
    suspend fun maxOrder(day: Int, section: String): Int?
}
