package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val name: String,
    val words: List<String>,
    val difficulty: Int
)
