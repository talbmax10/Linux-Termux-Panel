package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.model.Command
import com.example.linuxtermuxpanel.data.repository.CommandRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CommandViewModel @Inject constructor(
    private val commandRepository: CommandRepository
) : ViewModel() {
    private val _commands = MutableStateFlow<List<Command>>(emptyList())
    val commands: StateFlow<List<Command>> = _commands.asStateFlow()

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
}
