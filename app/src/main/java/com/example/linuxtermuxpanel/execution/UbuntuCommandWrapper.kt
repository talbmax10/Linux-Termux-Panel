package com.example.linuxtermuxpanel.execution

/**
 * Wraps a command to be executed in Ubuntu inside Termux.
 *
 * If the environment is Ubuntu, the command will be wrapped as:
 *   [ubuntuLoginCommand] -- bash -lc "[command]"
 *
 * If the environment is Termux, the command is used as-is.
 */
class UbuntuCommandWrapper(
    private val ubuntuLoginCommand: String
) {
    /**
     * Wraps the given command for the specified environment.
     *
     * @param command The command to execute.
     * @param environment Either "Termux" or "Ubuntu".
     * @return The wrapped command.
     */
    fun wrap(command: String, environment: String): String {
        return if (environment.equals("Ubuntu", ignoreCase = true)) {
            // We need to properly escape the command for bash -lc.
            // We'll wrap the command in single quotes and escape any existing single quotes.
            val escapedCommand = command.replace("'", "'\\''")
            "$ubuntuLoginCommand -- bash -lc '$escapedCommand'"
        } else {
            command
        }
    }
}