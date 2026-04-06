package com.dailyrap.app.di

import android.content.Context
import androidx.room.Room
import com.dailyrap.app.data.local.AppDatabase
import com.dailyrap.app.data.local.DailyResultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dailyrap.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideDailyResultDao(database: AppDatabase): DailyResultDao {
        return database.dailyResultDao()
    }
}
