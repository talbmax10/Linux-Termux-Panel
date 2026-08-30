package com.example.linuxtermuxpanel.execution

import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Executes commands through the Termux RUN_COMMAND intent. */
class FileBasedTermuxExecutor(
    private val context: Context,
    private val termuxPackageName: String,
    private val timeoutSeconds: Long
) : TermuxCommandExecutor {

    companion object {
        private const val TAG = "FileBasedTermuxExecutor"
        private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
        private const val EXTRA_COMMAND = "com.termux.RUN_COMMAND_PATH"
        private const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
        private const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    }

    override suspend fun execute(command: String): ExecutionResult = withContext(Dispatchers.IO) {
        if (command.isBlank()) return@withContext ExecutionResult(error = "Empty command", exitCode = -1)

        val cacheDir = context.cacheDir
        val uuid = UUID.randomUUID().toString()
        val commandFile = File(cacheDir, "command_$uuid.sh")
        val outputFile = File(cacheDir, "output_$uuid.txt")
        val errorFile = File(cacheDir, "error_$uuid.txt")
        val exitCodeFile = File(cacheDir, "exitcode_$uuid.txt")

        try {
            commandFile.writeText("$command\nexitCode=\$?\nprintf '%s' \"\$exitCode\" > '${exitCodeFile.absolutePath}'\n")

            val script = "sh '${commandFile.absolutePath}' > '${outputFile.absolutePath}' 2> '${errorFile.absolutePath}'"
            val intent = Intent(ACTION_RUN_COMMAND).apply {
                setPackage(termuxPackageName)
                putExtra(EXTRA_COMMAND, "/data/data/com.termux/files/usr/bin/sh")
                putExtra(EXTRA_ARGUMENTS, arrayOf("-c", script))
                putExtra(EXTRA_BACKGROUND, true)
            }
            context.sendBroadcast(intent)

            val timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds.coerceAtLeast(1))
            var elapsed = 0L
            while (!exitCodeFile.exists() && elapsed < timeoutMillis) {
                delay(100)
                elapsed += 100
            }

            if (!exitCodeFile.exists()) {
                return@withContext ExecutionResult(
                    output = outputFile.takeIf { it.exists() }?.readText().orEmpty(),
                    error = "Command execution timed out after $timeoutSeconds seconds",
                    exitCode = -1
                )
            }

            ExecutionResult(
                output = outputFile.takeIf { it.exists() }?.readText().orEmpty(),
                error = errorFile.takeIf { it.exists() }?.readText().orEmpty(),
                exitCode = exitCodeFile.readText().trim().toIntOrNull() ?: -1
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            ExecutionResult(error = e.toString(), exitCode = -1)
        } finally {
            listOf(commandFile, outputFile, errorFile, exitCodeFile).forEach { it.delete() }
        }
    }
}
