package com.example.linuxtermuxpanel.execution

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Implementation of TermuxCommandExecutor that uses the Termux RUN_COMMAND intent
 * and redirects output to files in the app's cache directory to capture the result.
 */
class FileBasedTermuxExecutor(
    private val context: Context,
    private val termuxPackageName: String,
    private val timeoutSeconds: Long
) : TermuxCommandExecutor {

    companion object {
        private const string TAG = "FileBasedTermuxExecutor"
        private const string ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
        private const string EXTRA_COMMAND = "command"
    }

    override suspend fun execute(command: String): ExecutionResult {
        return withContext(Dispatchers.IO) {
            if (command.isBlank()) {
                return@withContext ExecutionResult(
                    output = "",
                    error = "Empty command",
                    exitCode = -1
                )
            }

            val cacheDir = context.cacheDir
            if (!cacheDir.canWrite()) {
                return@withContext ExecutionResult(
                    output = "",
                    error = "Cache directory is not writable",
                    exitCode = -1
                )
            }

            val uuid = UUID.randomUUID().toString()
            val commandFile = File(cacheDir, "command.sh_$uuid")
            val outputFile = File(cacheDir, "output.txt_$uuid")
            val errorFile = File(cacheDir, "error.txt_$uuid")
            val exitCodeFile = File(cacheDir, "exitcode.txt_$uuid")

            try {
                // Write the command to the command file
                commandFile.writeText(command)
                // Make it executable
                commandFile.setExecutable(true, false /* ownerOnly */)

                // Construct the command to run via Termux:
                //   sh /path/to/command.sh >/path/to/output.txt 2>/path/to/error.txt
                val termuxCommand = "sh $commandFile.getAbsolutePath() >${outputFile.absolutePath} 2>${errorFile.absolutePath}"
                Log.d(TAG, "Executing termux command: $termuxCommand")

                // Send the RUN_COMMAND intent
                val intent = Intent(ACTION_RUN_COMMAND).apply {
                    setPackage(termuxPackageName)
                    putExtra(EXTRA_COMMAND, termuxCommand)
                }
                context.startActivity(intent)

                // Wait for the exit code file to appear, with a timeout
                var elapsedMillis = 0L
                val pollInterval = 100L // ms
                val timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds)
                while (!exitCodeFile.exists() && elapsedMillis < timeoutMillis) {
                    delay(pollInterval)
                    elapsedMillis += pollInterval
                }

                if (!exitCodeFile.exists()) {
                    // Timeout
                    return@withContext ExecutionResult(
                        output = "",
                        error = "Command execution timed out after $timeoutSeconds seconds",
                        exitCode = -1
                    )
                }

                // Read the results
                val exitCode = exitCodeFile.readText().trim().toIntOrNull() ?: -1
                val output = if (outputFile.exists()) outputFile.readText() else ""
                val error = if (errorFile.exists()) errorFile.readText() else ""

                return@withContext ExecutionResult(
                    output = output,
                    error = error,
                    exitCode = exitCode
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error executing command: $command", e)
                return@withContext ExecutionResult(
                    output = "",
                    error = e.toString(),
                    exitCode = -1
                )
            } finally {
                // Clean up the temporary files
                listOf(commandFile, outputFile, errorFile, exitCodeFile).forEach { it.delete() }
            }
        }
    }
}