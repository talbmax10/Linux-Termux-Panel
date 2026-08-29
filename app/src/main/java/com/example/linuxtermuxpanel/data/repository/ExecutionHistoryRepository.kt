package com.example.linuxtermuxpanel.data.repository

import com.example.linuxtermuxpanel.data.local.ExecutionHistoryDao
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExecutionHistoryRepository @Inject constructor(
    private val executionHistoryDao: ExecutionHistoryDao
) {
    suspend fun getAllExecutionHistory(): List<ExecutionHistory> = executionHistoryDao.getAllExecutionHistory()

    suspend fun getExecutionHistoryById(id: Long): ExecutionHistory = executionHistoryDao.getExecutionHistoryById(id)

    suspend fun insertExecutionHistory(history: ExecutionHistory): Long = executionHistoryDao.insertExecutionHistory(history)

    suspend fun updateExecutionHistory(history: ExecutionHistory) = executionHistoryDao.updateExecutionHistory(history)

    suspend fun deleteExecutionHistory(history: ExecutionHistory) = executionHistoryDao.deleteExecutionHistory(history)

    suspend fun deleteAllExecutionHistory() = executionHistoryDao.deleteAllExecutionHistory()
}