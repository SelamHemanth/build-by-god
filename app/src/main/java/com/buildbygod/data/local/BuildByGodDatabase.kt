package com.buildbygod.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.buildbygod.data.local.dao.ExerciseDao
import com.buildbygod.data.local.dao.IntakeDao
import com.buildbygod.data.local.dao.ProgressDao
import com.buildbygod.data.local.dao.WorkoutDao
import com.buildbygod.data.local.entity.BodyWeightEntity
import com.buildbygod.data.local.entity.DayExerciseCrossRef
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.local.entity.IntakeEntity
import com.buildbygod.data.local.entity.SessionLogEntity
import com.buildbygod.data.local.entity.WorkoutDayEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutDayEntity::class,
        DayExerciseCrossRef::class,
        SessionLogEntity::class,
        BodyWeightEntity::class,
        IntakeEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class BuildByGodDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun progressDao(): ProgressDao
    abstract fun intakeDao(): IntakeDao

    companion object {
        const val NAME = "buildbygod.db"
    }
}
