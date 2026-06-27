package com.buildbygod.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A completed (or partially completed) workout session for the progress screen. */
@Entity(tableName = "session_logs")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Epoch day (LocalDate.toEpochDay) the session happened. */
    val epochDay: Long,
    val dayOfWeek: Int,
    val title: String,
    val completedCount: Int,
    val totalCount: Int,
    val durationSeconds: Long
)
