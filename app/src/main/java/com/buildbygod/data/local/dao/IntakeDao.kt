package com.buildbygod.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.buildbygod.data.local.entity.IntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {

    @Insert
    suspend fun insert(entry: IntakeEntity): Long

    @Query("DELETE FROM intake_log WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM intake_log WHERE epochDay = :day ORDER BY id DESC")
    fun observeForDay(day: Long): Flow<List<IntakeEntity>>

    @Query("SELECT DISTINCT epochDay FROM intake_log ORDER BY epochDay DESC")
    fun observeLoggedDays(): Flow<List<Long>>
}
