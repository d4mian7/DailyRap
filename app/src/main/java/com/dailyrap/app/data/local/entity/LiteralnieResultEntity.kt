package com.dailyrap.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "literalnie_results")
data class LiteralnieResultEntity(
    @PrimaryKey val date: String,
    val wordId: Int,
    val won: Boolean,
    val guessCount: Int,
    val variant: Int,
    val guessHistory: String
)
