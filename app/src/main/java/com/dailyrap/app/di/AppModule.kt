package com.dailyrap.app.di

import android.content.Context
import com.dailyrap.app.data.repository.CrosswordRepository
import com.dailyrap.app.data.repository.GameProgressRepository
import com.dailyrap.app.data.repository.PuzzleRepository
import com.dailyrap.app.data.repository.WordRepository
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
    fun providePuzzleRepository(
        @ApplicationContext context: Context
    ): PuzzleRepository {
        return PuzzleRepository(context)
    }

    @Provides
    @Singleton
    fun provideWordRepository(
        @ApplicationContext context: Context
    ): WordRepository {
        return WordRepository(context)
    }

    @Provides
    @Singleton
    fun provideCrosswordRepository(
        @ApplicationContext context: Context
    ): CrosswordRepository {
        return CrosswordRepository(context)
    }

    @Provides
    @Singleton
    fun provideGameProgressRepository(
        @ApplicationContext context: Context
    ): GameProgressRepository {
        return GameProgressRepository(context)
    }
}
