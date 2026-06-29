package com.buildbygod.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One logged food/supplement the user actually consumed on a given day.
 * [foodId] references a [com.buildbygod.domain.model.FoodCatalog] item; macros are resolved at runtime.
 */
@Entity(tableName = "intake_log", indices = [Index("epochDay")])
data class IntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val foodId: String,
    val grams: Int
)
