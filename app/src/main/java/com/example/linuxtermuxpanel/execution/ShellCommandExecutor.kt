package com.example.linuxtermuxpanel.execution

import android.util.Log
import java.io.BufferedReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ينفّذ الأوامر داخل شِل أندرويد باستخدام ProcessBuilder.
 * يُستخدم كخطة بديلة عندما لا يكون Termux مثبّتًا (إمكانياته محدودة داخل صندوق التطبيق).
 */
@Singleton
class ShellCommandExecutor @Inject constructor() {

    suspend fun execute(command: String, timeoutSeconds: Long): ExecutionResult =
        withContext(Dispatchers.IO) { executeBlocking(command, timeoutSeconds) }

    private fun executeBlocking(command: String, timeoutSeconds: Long): ExecutionResult {
        if (command.isBlank()) {
            return ExecutionResult(error = "الأمر فارغ", exitCode = -1)
        }

        var process: Process? = null
        return try {
            process = ProcessBuilder()
                .command("sh", "-c", command)
                .redirectErrorStream(false)
                .start()

            val outputBuilder = StringBuilder()
            val errorBuilder = StringBuilder()

            val outputThread = readerThread(process.inputStream.reader(), outputBuilder)
            val errorThread = readerThread(process.errorStream.reader(), errorBuilder)
            outputThread.start()
            errorThread.start()

            val exited = process.waitFor(
                TimeUnit.SECONDS.toMillis(timeoutSeconds.coerceAtLeast(1L)),
                TimeUnit.MILLISECONDS
            )

            outputThread.join(1_000)
            errorThread.join(1_000)

            if (!exited) {
                process.destroyForcibly()
                return ExecutionResult(
                    output = outputBuilder.toString().trim(),
                    error = "انتهت مهلة تنفيذ الأمر بعد $timeoutSeconds ثانية.",
                    exitCode = -1
                )
            }

            ExecutionResult(
                output = outputBuilder.toString().trim(),
                error = errorBuilder.toString().trim(),
                exitCode = process.exitValue()
            )
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء تنفيذ الأمر: $command", e)
            ExecutionResult(error = "خطأ: ${e.message ?: e.javaClass.simpleName}", exitCode = -1)
        } finally {
            process?.destroy()
        }
    }

    private fun readerThread(reader: java.io.Reader, builder: StringBuilder): Thread = Thread {
        try {
            BufferedReader(reader).useLines { lines ->
                lines.forEach { line ->
                    synchronized(builder) {
                        if (builder.isNotEmpty()) builder.append('\n')
                        builder.append(line)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء قراءة مخرجات الأمر", e)
        }
    }

    companion object {
        private const val TAG = "ShellCommandExecutor"
    }
}
