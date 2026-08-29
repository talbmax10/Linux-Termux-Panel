package com.example.linuxtermuxpanel.execution

/**
 * Interface for executing commands via Termux.
 */
interface TermuxCommandExecutor {
    /**
     * Executes a command in Termux.
     *
     * @param command The command to execute.
     * @return The result of the execution, including output, error, and exit code.
     */
    suspend fun execute(command: String): ExecutionResult
}

/**
 * Result of executing a command.
 */
data class ExecutionResult(
    val output: String = "",
    val error: String = "",
    val exitCode: Int = -1
)