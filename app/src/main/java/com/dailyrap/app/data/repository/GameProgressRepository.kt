package com.dailyrap.app.data.repository

import android.content.Context
import com.dailyrap.app.data.model.ConnectionsGameState
import com.dailyrap.app.data.model.CrosswordGameState
import com.dailyrap.app.data.model.LiteralnieGameState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameProgressRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs by lazy {
        context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
    }

    // ── Literalnie ──────────────────────────────────────────

    fun saveLiteralnieProgress(state: LiteralnieGameState) {
        prefs.edit()
            .putString("literalnie_state", json.encodeToString(state))
            .putString("literalnie_date", state.dailyWord.date)
            .apply()
    }

    fun loadLiteralnieProgress(date: String): LiteralnieGameState? {
        val savedDate = prefs.getString("literalnie_date", null) ?: return null
        if (savedDate != date) return null
        val stateJson = prefs.getString("literalnie_state", null) ?: return null
        return try {
            json.decodeFromString<LiteralnieGameState>(stateJson)
        } catch (_: Exception) {
            null
        }
    }

    // ── Connections ──────────────────────────────────────────

    fun saveConnectionsProgress(state: ConnectionsGameState, elapsedSeconds: Int) {
        prefs.edit()
            .putString("connections_state", json.encodeToString(state))
            .putString("connections_date", state.puzzle.date)
            .putInt("connections_elapsed", elapsedSeconds)
            .apply()
    }

    fun loadConnectionsProgress(date: String): Pair<ConnectionsGameState, Int>? {
        val savedDate = prefs.getString("connections_date", null) ?: return null
        if (savedDate != date) return null
        val stateJson = prefs.getString("connections_state", null) ?: return null
        val elapsed = prefs.getInt("connections_elapsed", 0)
        return try {
            json.decodeFromString<ConnectionsGameState>(stateJson) to elapsed
        } catch (_: Exception) {
            null
        }
    }

    // ── Crossword ──────────────────────────────────────────

    fun saveCrosswordProgress(state: CrosswordGameState) {
        prefs.edit()
            .putString("crossword_state", json.encodeToString(state))
            .putString("crossword_date", state.puzzle.date)
            .apply()
    }

    fun loadCrosswordProgress(date: String): CrosswordGameState? {
        val savedDate = prefs.getString("crossword_date", null) ?: return null
        if (savedDate != date) return null
        val stateJson = prefs.getString("crossword_state", null) ?: return null
        return try {
            json.decodeFromString<CrosswordGameState>(stateJson)
        } catch (_: Exception) {
            null
        }
    }
}
