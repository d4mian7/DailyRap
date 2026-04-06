package com.dailyrap.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections_results")
data class ConnectionsResultEntity(
    @PrimaryKey val date: String,
    val puzzleId: Int,
    val won: Boolean,
    val mistakes: Int,
    val timeSeconds: Int,
    val guessHistory: String
)
