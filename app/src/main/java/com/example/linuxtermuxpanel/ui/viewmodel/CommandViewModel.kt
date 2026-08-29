package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.repository.CommandRepository
import com.example.linuxtermuxpanel.data.model.Command
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CommandViewModel @Inject constructor(
    private val commandRepository: CommandRepository
) : ViewModel() {

    private val _commands = MutableStateFlow<List<Command>>(emptyList())
    val commands: StateFlow<List<Command>> = _commands

    init {
        loadCommands()
    }

    private fun loadCommands() {
        viewModelScope.launch {
            val commands = commandRepository.getAllCommands()
            _commands.value = commands
        }
    }

    fun addCommand(command: Command) = viewModelScope.launch {
        commandRepository.insertCommand(command)
        loadCommands()
    }

    fun updateCommand(command: Command) = viewModelScope.launch {
        commandRepository.updateCommand(command)
        loadCommands()
    }

    fun deleteCommand(command: Command) = viewModelScope.launch {
        commandRepository.deleteCommand(command)
        loadCommands()
    }

    fun deleteAllCommands() = viewModelScope.launch {
        commandRepository.deleteAllCommands()
        loadCommands()
    }
}