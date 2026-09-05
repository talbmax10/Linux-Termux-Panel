package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.model.Command
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import com.example.linuxtermuxpanel.data.repository.CommandRepository
import com.example.linuxtermuxpanel.data.repository.ExecutionHistoryRepository
import com.example.linuxtermuxpanel.execution.CommandExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CommandViewModel @Inject constructor(
    private val commandRepository: CommandRepository,
    private val executionHistoryRepository: ExecutionHistoryRepository,
    private val commandExecutor: CommandExecutor
) : ViewModel() {

    private val _commands = MutableStateFlow<List<Command>>(emptyList())
    val commands: StateFlow<List<Command>> = _commands.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _lastExecutionResult = MutableStateFlow<ExecutionResultUiState?>(null)
    val lastExecutionResult: StateFlow<ExecutionResultUiState?> = _lastExecutionResult.asStateFlow()

    init { loadCommands() }

    private fun loadCommands() {
        viewModelScope.launch {
            commandRepository.getAllCommands().collect { _commands.value = it }
        }
    }

    fun addCommand(command: Command) = viewModelScope.launch { commandRepository.insertCommand(command) }

    fun updateCommand(command: Command) = viewModelScope.launch { commandRepository.updateCommand(command) }

    fun deleteCommand(command: Command) = viewModelScope.launch { commandRepository.deleteCommand(command) }

    fun deleteAllCommands() = viewModelScope.launch { commandRepository.deleteAllCommands() }

    fun executeCommand(command: Command) {
        if (_isExecuting.value) return

        viewModelScope.launch {
            _isExecuting.value = true
            _lastExecutionResult.value = null

            val startedAt = Date()

            try {
                // Use executeWithEnvironment to properly wrap Ubuntu commands
                val result = commandExecutor.executeWithEnvironment(command.command, command.environment)

                val history = ExecutionHistory(
                    commandId = command.id,
                    commandText = command.command,
                    output = result.output,
                    error = result.error,
                    exitCode = result.exitCode,
                    startedAt = startedAt,
                    finishedAt = Date(),
                    success = result.exitCode == 0
                )

                executionHistoryRepository.insertExecutionHistory(history)

                _lastExecutionResult.value = ExecutionResultUiState(
                    success = result.exitCode == 0,
                    output = result.output ?: "",
                    error = result.error ?: "",
                    exitCode = result.exitCode
                )
            } catch (e: Exception) {
                val history = ExecutionHistory(
                    commandId = command.id,
                    commandText = command.command,
                    error = e.message ?: "Unknown error",
                    exitCode = -1,
                    startedAt = startedAt,
                    finishedAt = Date(),
                    success = false
                )

                executionHistoryRepository.insertExecutionHistory(history)

                _lastExecutionResult.value = ExecutionResultUiState(
                    success = false,
                    output = "",
                    error = e.message ?: "Unknown error",
                    exitCode = -1
                )
            } finally {
                _isExecuting.value = false
            }
        }
    }

    fun clearExecutionResult() {
        _lastExecutionResult.value = null
    }
}

data class ExecutionResultUiState(
    val success: Boolean,
    val output: String,
    val error: String,
    val exitCode: Int
)
