package com.buildbygod.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One planned workout per day-of-week. dayOfWeek uses java.time.DayOfWeek value (1=Mon..7=Sun).
 * scheduledMinutes is minutes from midnight (e.g. 18*60 = 6pm). -1 means no time set.
 */
@Entity(tableName = "workout_days")
data class WorkoutDayEntity(
    @PrimaryKey val dayOfWeek: Int,
    val title: String,
    val focus: String,
    val scheduledMinutes: Int = -1,
    val reminderEnabled: Boolean = false,
    val isRestDay: Boolean = false
)
