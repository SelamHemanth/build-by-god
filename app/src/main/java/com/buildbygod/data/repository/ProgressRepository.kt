package com.buildbygod.data.repository

import com.buildbygod.data.local.dao.ProgressDao
import com.buildbygod.data.local.entity.BodyWeightEntity
import com.buildbygod.data.local.entity.SessionLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val dao: ProgressDao
) {
    fun sessions(): Flow<List<SessionLogEntity>> = dao.observeSessions()
    fun sessionDays(): Flow<List<Long>> = dao.observeSessionDays()
    fun bodyWeight(): Flow<List<BodyWeightEntity>> = dao.observeBodyWeight()

    suspend fun logSession(log: SessionLogEntity) = dao.insertSession(log)
    suspend fun logBodyWeight(entry: BodyWeightEntity) = dao.upsertBodyWeight(entry)
}
