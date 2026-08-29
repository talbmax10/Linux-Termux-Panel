package com.example.linuxtermuxpanel.data.repository

import com.example.linuxtermuxpanel.data.local.CommandDao
import com.example.linuxtermuxpanel.data.local.CommandDatabase
import com.example.linuxtermuxpanel.data.model.Command
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CommandRepository @Inject constructor(
    private val commandDao: CommandDao
) {
    suspend fun getAllCommands(): List<Command> = commandDao.getAllCommands()

    suspend fun getCommandById(id: Long): Command = commandDao.getCommandById(id)

    suspend fun insertCommand(command: Command): Long = commandDao.insertCommand(command)

    suspend fun updateCommand(command: Command) = commandDao.updateCommand(command)

    suspend fun deleteCommand(command: Command) = commandDao.deleteCommand(command)

    suspend fun deleteAllCommands() = commandDao.deleteAllCommands()
}