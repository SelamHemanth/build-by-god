package com.buildbygod.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.buildbygod.data.local.entity.BodyWeightEntity
import com.buildbygod.data.local.entity.SessionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(log: SessionLogEntity): Long

    @Query("SELECT * FROM session_logs ORDER BY epochDay DESC, id DESC")
    fun observeSessions(): Flow<List<SessionLogEntity>>

    @Query("SELECT DISTINCT epochDay FROM session_logs ORDER BY epochDay DESC")
    fun observeSessionDays(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBodyWeight(entry: BodyWeightEntity)

    @Query("SELECT * FROM body_weight ORDER BY epochDay")
    fun observeBodyWeight(): Flow<List<BodyWeightEntity>>
}
