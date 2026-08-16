package com.dailyrap.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dailyrap.app.data.local.entity.ConnectionsResultEntity
import com.dailyrap.app.data.local.entity.CrosswordResultEntity
import com.dailyrap.app.data.local.entity.LiteralnieResultEntity

@Database(
    entities = [ConnectionsResultEntity::class, LiteralnieResultEntity::class, CrosswordResultEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyResultDao(): DailyResultDao
}
