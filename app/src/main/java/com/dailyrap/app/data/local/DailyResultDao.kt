package com.dailyrap.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailyrap.app.data.local.entity.ConnectionsResultEntity
import com.dailyrap.app.data.local.entity.CrosswordResultEntity
import com.dailyrap.app.data.local.entity.LiteralnieResultEntity

@Dao
interface DailyResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectionsResult(result: ConnectionsResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiteralnieResult(result: LiteralnieResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrosswordResult(result: CrosswordResultEntity)

    @Query("SELECT * FROM connections_results WHERE date = :date")
    suspend fun getConnectionsResult(date: String): ConnectionsResultEntity?

    @Query("SELECT * FROM literalnie_results WHERE date = :date")
    suspend fun getLiteralnieResult(date: String): LiteralnieResultEntity?

    @Query("SELECT * FROM crossword_results WHERE date = :date")
    suspend fun getCrosswordResult(date: String): CrosswordResultEntity?

    @Query("SELECT * FROM connections_results ORDER BY date DESC")
    suspend fun getAllConnectionsResults(): List<ConnectionsResultEntity>

    @Query("SELECT * FROM literalnie_results ORDER BY date DESC")
    suspend fun getAllLiteralnieResults(): List<LiteralnieResultEntity>

    @Query("SELECT * FROM crossword_results ORDER BY date DESC")
    suspend fun getAllCrosswordResults(): List<CrosswordResultEntity>

    @Query("DELETE FROM connections_results")
    suspend fun deleteAllConnectionsResults()

    @Query("DELETE FROM literalnie_results")
    suspend fun deleteAllLiteralnieResults()

    @Query("DELETE FROM crossword_results")
    suspend fun deleteAllCrosswordResults()
}
