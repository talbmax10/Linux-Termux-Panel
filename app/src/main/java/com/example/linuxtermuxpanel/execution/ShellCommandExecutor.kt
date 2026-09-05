package com.example.linuxtermuxpanel.execution

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes commands directly in Android shell using ProcessBuilder.
 * This doesn't require Termux bridge but also doesn't run in Termux environment.
 * 
 * For Termux-specific features, a Termux bridge app is required.
 */
@Singleton
class ShellCommandExecutor @Inject constructor() : TermuxCommandExecutor {

    companion object {
        private const val TAG = "ShellCommandExecutor"
        private const val DEFAULT_TIMEOUT_SECONDS = 30L
    }

    override suspend fun execute(command: String): ExecutionResult = withContext(Dispatchers.IO) {
        executeWithTimeout(command, DEFAULT_TIMEOUT_SECONDS)
    }

    private fun executeWithTimeout(command: String, timeoutSeconds: Long): ExecutionResult {
        if (command.isBlank()) {
            return ExecutionResult(error = "Empty command", exitCode = -1)
        }

        var process: Process? = null
        try {
            val processBuilder = ProcessBuilder()
                .command("sh", "-c", command)
                .redirectErrorStream(false)

            process = processBuilder.start()

            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val outputBuilder = StringBuilder()
            val errorBuilder = StringBuilder()

            val outputThread = Thread {
                try {
                    var line: String?
                    while (outputReader.readLine().also { line = it } != null) {
                        if (outputBuilder.isNotEmpty()) outputBuilder.append("\n")
                        outputBuilder.append(line)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading output", e)
                }
            }

            val errorThread = Thread {
                try {
                    var line: String?
                    while (errorReader.readLine().also { line = it } != null) {
                        if (errorBuilder.isNotEmpty()) errorBuilder.append("\n")
                        errorBuilder.append(line)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading error", e)
                }
            }

            outputThread.start()
            errorThread.start()

            val timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds.coerceAtLeast(1))
            val exited = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)

            outputThread.join(1000)
            errorThread.join(1000)

            if (!exited) {
                process.destroyForcibly()
                return ExecutionResult(
                    output = outputBuilder.toString(),
                    error = "Command timed out after $timeoutSeconds seconds",
                    exitCode = -1
                )
            }

            val exitCode = process.exitValue()

            ExecutionResult(
                output = outputBuilder.toString(),
                error = errorBuilder.toString(),
                exitCode = exitCode
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command: $command", e)
            return ExecutionResult(
                error = "Error: ${e.message}",
                exitCode = -1
            )
        } finally {
            process?.destroy()
        }
    }
}
