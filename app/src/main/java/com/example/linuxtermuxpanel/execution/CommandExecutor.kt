package com.example.linuxtermuxpanel.execution

import com.example.linuxtermuxpanel.ui.viewmodel.Settings
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Command executor that wraps commands for different environments.
 * Uses the injected TermuxCommandExecutor for actual execution.
 */
class CommandExecutor @Inject constructor(
    private val termuxExecutor: TermuxCommandExecutor
) : TermuxCommandExecutor {

    // Default settings - in a real app, this would come from DataStore
    private val settings = Settings()

    override suspend fun execute(command: String): ExecutionResult =
        termuxExecutor.execute(command)

    suspend fun executeWithEnvironment(command: String, environment: String): ExecutionResult {
        val finalCommand = if (environment.equals("Ubuntu", ignoreCase = true)) {
            UbuntuCommandWrapper(settings.ubuntuLoginCommand).wrap(command, "Ubuntu")
        } else {
            command
        }
        return termuxExecutor.execute(finalCommand)
    }
}
