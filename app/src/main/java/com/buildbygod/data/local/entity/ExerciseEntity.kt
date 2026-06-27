package com.buildbygod.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** ExerciseType name: WARMUP / MAIN / STRETCH */
    val type: String,
    /** MuscleGroup name */
    val muscleGroup: String,
    /** Equipment name */
    val equipment: String,
    /** Newline-separated step-by-step instructions. */
    val instructions: String,
    val tips: String,
    val defaultSets: Int,
    val defaultReps: String,
    /** Duration in seconds for timed moves (warmups/stretches/cardio). 0 = rep based. */
    val durationSeconds: Int,
    /** Asset filename for a bundled looping demo clip/gif, e.g. "clips/pushup.gif". Nullable. */
    val clipAsset: String?,
    /** Optional YouTube link for the full demo video. */
    val youtubeUrl: String?,
    val isFavorite: Boolean = false
)
