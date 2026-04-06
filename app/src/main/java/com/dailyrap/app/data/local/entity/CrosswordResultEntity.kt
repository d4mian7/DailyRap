package com.dailyrap.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crossword_results")
data class CrosswordResultEntity(
    @PrimaryKey val date: String,
    val puzzleId: Int,
    val correctAnswers: Int,
    val totalClues: Int,
    val secretWordFound: Boolean
)
