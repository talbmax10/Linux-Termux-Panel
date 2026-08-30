package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import com.example.linuxtermuxpanel.data.repository.ExecutionHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ExecutionHistoryViewModel @Inject constructor(
    private val executionHistoryRepository: ExecutionHistoryRepository
) : ViewModel() {
    private val _executionHistory = MutableStateFlow<List<ExecutionHistory>>(emptyList())
    val executionHistory: StateFlow<List<ExecutionHistory>> = _executionHistory.asStateFlow()

    init { loadExecutionHistory() }

    private fun loadExecutionHistory() {
        viewModelScope.launch {
            executionHistoryRepository.getAllExecutionHistory().collect { _executionHistory.value = it }
        }
    }

    fun addExecutionHistory(history: ExecutionHistory) = viewModelScope.launch { executionHistoryRepository.insertExecutionHistory(history) }
    fun updateExecutionHistory(history: ExecutionHistory) = viewModelScope.launch { executionHistoryRepository.updateExecutionHistory(history) }
    fun deleteExecutionHistory(history: ExecutionHistory) = viewModelScope.launch { executionHistoryRepository.deleteExecutionHistory(history) }
    fun deleteAllExecutionHistory() = viewModelScope.launch { executionHistoryRepository.deleteAllExecutionHistory() }
}
