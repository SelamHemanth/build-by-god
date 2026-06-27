package com.buildbygod.data.repository

import com.buildbygod.data.local.dao.ExerciseDao
import com.buildbygod.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val dao: ExerciseDao
) {
    fun all(): Flow<List<ExerciseEntity>> = dao.observeAll()
    fun byId(id: String): Flow<ExerciseEntity?> = dao.observeById(id)
    fun byType(type: String): Flow<List<ExerciseEntity>> = dao.observeByType(type)
    fun favorites(): Flow<List<ExerciseEntity>> = dao.observeFavorites()
    suspend fun setFavorite(id: String, fav: Boolean) = dao.setFavorite(id, fav)
}
