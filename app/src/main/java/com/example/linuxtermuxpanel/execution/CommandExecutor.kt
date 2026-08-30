package com.example.linuxtermuxpanel.execution

import android.content.Context
import com.example.linuxtermuxpanel.ui.settings.Settings

class CommandExecutor(
    context: Context,
    private val settings: Settings
) : TermuxCommandExecutor {
    private val termuxExecutor: TermuxCommandExecutor

    init {
        val termuxPackageName = settings.termuxPackageName.ifEmpty { "com.termux" }
        val timeoutSeconds = if (settings.timeoutSeconds > 0) settings.timeoutSeconds else 30
        termuxExecutor = FileBasedTermuxExecutor(context, termuxPackageName, timeoutSeconds)
    }

    override suspend fun execute(command: String): ExecutionResult =
        termuxExecutor.execute(command)

    suspend fun executeWithEnvironment(command: String, environment: String): ExecutionResult {
        val finalCommand = if (environment.equals("Ubuntu", ignoreCase = true)) {
            UbuntuCommandWrapper(settings.ubuntuLoginCommand).wrap(command)
        } else {
            command
        }
        return termuxExecutor.execute(finalCommand)
    }
}
