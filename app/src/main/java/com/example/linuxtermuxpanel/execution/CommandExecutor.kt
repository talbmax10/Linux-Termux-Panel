package com.example.linuxtermuxpanel.execution

import android.content.Context
import com.example.linuxtermuxpanel.ui.settings.Settings

/**
 * Executes commands in either Termux or Ubuntu (via Termux).
 *
 * This executor uses the Settings to determine the Termux package name and timeout.
 * For Ubuntu commands, it wraps the command with the Ubuntu login command.
 */
class CommandExecutor(
    private val context: Context,
    private val settings: Settings
) : TermuxCommandExecutor {

    private val termuxExecutor: TermuxCommandExecutor

    init {
        // Use the FileBasedTermuxExecutor with the settings
        val termuxPackageName = settings.termuxPackageName.isNotEmpty()
            ? settings.termuxPackageName
            : "com.termux" // fallback to default
        val timeoutSeconds = if (settings.timeoutSeconds > 0) settings.timeoutSeconds else 30
        termuxExecutor = FileBasedTermuxExecutor(context, termuxPackageName, timeoutSeconds)
    }

    override suspend fun execute(command: String): ExecutionResult {
        // This method is not used directly; we use executeWithEnvironment instead.
        // But we must implement it because we inherit from TermuxCommandExecutor.
        // We'll delegate to the termuxExecutor with the given command (assuming Termux environment).
        return termuxExecutor.execute(command)
    }

    /**
     * Executes a command in the specified environment.
     *
     * @param command The command to execute.
     * @param environment Either "Termux" or "Ubuntu".
     * @return The result of the execution.
     */
    suspend fun executeWithEnvironment(command: String, environment: String): ExecutionResult {
        val finalCommand = if (environment.equals("Ubuntu", ignoreCase = true)) {
            // Wrap the command for Ubuntu
            UbuntuCommandWrapper(settings.ubuntuLoginCommand).wrap(command)
        } else {
            // For Termux, use the command as-is
            command
        }
        return termuxExecutor.execute(finalCommand)
    }
}