package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.repository.ExecutionHistoryRepository
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ExecutionHistoryViewModel @Inject constructor(
    private val executionHistoryRepository: ExecutionHistoryRepository
) : ViewModel() {

    private val _executionHistory = MutableStateFlow<List<ExecutionHistory>>(emptyList())
    val executionHistory: StateFlow<List<ExecutionHistory>> = _executionHistory

    init {
        loadExecutionHistory()
    }

    private fun loadExecutionHistory() {
        viewModelScope.launch {
            val history = executionHistoryRepository.getAllExecutionHistory()
            _executionHistory.value = history
        }
    }

    fun addExecutionHistory(history: ExecutionHistory) = viewModelScope.launch {
        executionHistoryRepository.insertExecutionHistory(history)
        loadExecutionHistory()
    }

    fun updateExecutionHistory(history: ExecutionHistory) = viewModelScope.launch {
        executionHistoryRepository.updateExecutionHistory(history)
        loadExecutionHistory()
    }

    fun deleteExecutionHistory(history: ExecutionHistory) = viewModelScope.launch {
        executionHistoryRepository.deleteExecutionHistory(history)
        loadExecutionHistory()
    }

    fun deleteAllExecutionHistory() = viewModelScope.launch {
        executionHistoryRepository.deleteAllExecutionHistory()
        loadExecutionHistory()
    }
}