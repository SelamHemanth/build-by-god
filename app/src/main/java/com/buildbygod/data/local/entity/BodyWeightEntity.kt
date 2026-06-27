package com.buildbygod.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Body-weight log entry, one per epoch day (latest wins). */
@Entity(tableName = "body_weight")
data class BodyWeightEntity(
    @PrimaryKey val epochDay: Long,
    val weight: Float
)
