package com.buildbygod.di

import android.content.Context
import androidx.room.Room
import com.buildbygod.data.local.BuildByGodDatabase
import com.buildbygod.data.local.dao.ExerciseDao
import com.buildbygod.data.local.dao.ProgressDao
import com.buildbygod.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BuildByGodDatabase =
        Room.databaseBuilder(context, BuildByGodDatabase::class.java, BuildByGodDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideExerciseDao(db: BuildByGodDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideWorkoutDao(db: BuildByGodDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideProgressDao(db: BuildByGodDatabase): ProgressDao = db.progressDao()
}
