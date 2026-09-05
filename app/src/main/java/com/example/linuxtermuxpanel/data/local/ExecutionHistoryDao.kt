package com.example.linuxtermuxpanel.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionHistoryDao {

    @Query("SELECT * FROM execution_history ORDER BY startedAt DESC")
    fun getAllExecutionHistory(): Flow<List<ExecutionHistory>>

    @Query("SELECT * FROM execution_history WHERE id = :historyId")
    fun getExecutionHistoryById(historyId: Long): ExecutionHistory

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecutionHistory(history: ExecutionHistory): Long

    @Update
    suspend fun updateExecutionHistory(history: ExecutionHistory)

    @Delete
    suspend fun deleteExecutionHistory(history: ExecutionHistory)

    @Query("DELETE FROM execution_history")
    suspend fun deleteAllExecutionHistory()
}