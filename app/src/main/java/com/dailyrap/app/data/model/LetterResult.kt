package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LetterResult {
    CORRECT,
    PRESENT,
    ABSENT
}
