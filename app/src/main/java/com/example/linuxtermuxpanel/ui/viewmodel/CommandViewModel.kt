package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.model.Command
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import com.example.linuxtermuxpanel.data.repository.CommandRepository
import com.example.linuxtermuxpanel.data.repository.ExecutionHistoryRepository
import com.example.linuxtermuxpanel.execution.CommandExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val _runningCommandId = MutableStateFlow<Long?>(null)
    val runningCommandId: StateFlow<Long?> = _runningCommandId.asStateFlow()

    private val _lastExecutionResult = MutableStateFlow<ExecutionResultUiState?>(null)
    val lastExecutionResult: StateFlow<ExecutionResultUiState?> = _lastExecutionResult.asStateFlow()

    init {
        loadCommands()
    }

    private fun loadCommands() {
        viewModelScope.launch {
            commandRepository.getAllCommands().collect { _commands.value = it }
        }
    }

    fun addCommand(command: Command) = viewModelScope.launch {
        commandRepository.insertCommand(command.copy(createdAt = Date(), updatedAt = Date()))
    }

    fun updateCommand(command: Command) = viewModelScope.launch {
        // كان التعديل سابقًا لا يحدّث حقل updatedAt
        commandRepository.updateCommand(command.copy(updatedAt = Date()))
    }

    fun deleteCommand(command: Command) = viewModelScope.launch {
        commandRepository.deleteCommand(command)
    }

    fun deleteAllCommands() = viewModelScope.launch { commandRepository.deleteAllCommands() }

    fun toggleFavorite(command: Command) = viewModelScope.launch {
        commandRepository.updateCommand(
            command.copy(isFavorite = !command.isFavorite, updatedAt = Date())
        )
    }

    fun executeCommand(command: Command) {
        if (_isExecuting.value) return

        viewModelScope.launch {
            _isExecuting.value = true
            _runningCommandId.value = command.id
            _lastExecutionResult.value = null

            val startedAt = Date()
            val result = try {
                commandExecutor.execute(
                    command = command.command,
                    environment = command.environment,
                    interactive = command.needsInteractiveTerminal
                )
            } catch (e: Exception) {
                com.example.linuxtermuxpanel.execution.ExecutionResult(
                    error = e.message ?: "خطأ غير معروف",
                    exitCode = -1
                )
            }

            executionHistoryRepository.insertExecutionHistory(
                ExecutionHistory(
                    commandId = command.id.takeIf { it > 0 },
                    label = command.name,
                    commandText = command.command,
                    output = result.output,
                    error = result.error,
                    exitCode = result.exitCode,
                    startedAt = startedAt,
                    finishedAt = Date(),
                    success = result.success
                )
            )

            _lastExecutionResult.value = ExecutionResultUiState(
                title = command.name,
                success = result.success,
                output = result.output,
                error = result.error,
                exitCode = result.exitCode
            )
            _runningCommandId.value = null
            _isExecuting.value = false
        }
    }

    fun clearExecutionResult() {
        _lastExecutionResult.value = null
    }
}

data class ExecutionResultUiState(
    val title: String,
    val success: Boolean,
    val output: String,
    val error: String,
    val exitCode: Int
)
