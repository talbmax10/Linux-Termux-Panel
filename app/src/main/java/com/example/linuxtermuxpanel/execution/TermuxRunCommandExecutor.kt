package com.example.linuxtermuxpanel.execution

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.linuxtermuxpanel.data.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * ينفّذ الأوامر عبر خدمة `RunCommandService` في Termux.
 *
 * ملاحظات مهمّة:
 *  - Termux يستقبل الأمر عبر [Context.startService] وليس عبر Broadcast.
 *  - نتيجة التنفيذ (stdout/stderr/exitCode) تعود عبر PendingIntent وليس عبر ملفات،
 *    لأن Termux لا يستطيع قراءة مجلد الكاش الخارجي للتطبيق على أندرويد 10+.
 *  - يجب تفعيل `allow-external-apps=true` في ملف `~/.termux/termux.properties`.
 */
@Singleton
class TermuxRunCommandExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isTermuxInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }

    suspend fun execute(
        command: String,
        settings: AppSettings,
        interactive: Boolean = false
    ): ExecutionResult {
        if (command.isBlank()) {
            return ExecutionResult(error = "الأمر فارغ", exitCode = -1)
        }
        if (!isTermuxInstalled(settings.termuxPackageName)) {
            return ExecutionResult(
                error = "تطبيق Termux (${settings.termuxPackageName}) غير مثبّت على الجهاز.",
                exitCode = -1
            )
        }
        return if (interactive) {
            runInteractive(command, settings)
        } else {
            runInBackground(command, settings)
        }
    }

    private fun runInteractive(command: String, settings: AppSettings): ExecutionResult {
        val intent = buildIntent(command, settings, background = false)
        val error = startTermuxService(intent)
        return if (error == null) {
            ExecutionResult(
                output = "تم فتح الأمر داخل جلسة Termux تفاعلية (لا يمكن التقاط المخرجات في هذا الوضع).",
                exitCode = 0
            )
        } else {
            ExecutionResult(error = error, exitCode = -1)
        }
    }

    private suspend fun runInBackground(command: String, settings: AppSettings): ExecutionResult {
        val timeoutSeconds = settings.timeoutSeconds.coerceIn(1, 3600)
        val resultAction = "${context.packageName}.TERMUX_RESULT.${UUID.randomUUID()}"

        val result = withTimeoutOrNull(TimeUnit.SECONDS.toMillis(timeoutSeconds.toLong())) {
            suspendCancellableCoroutine<ExecutionResult> { continuation ->
                val handled = AtomicBoolean(false)
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(receiverContext: Context?, intent: Intent?) {
                        if (!handled.compareAndSet(false, true)) return
                        unregister(this)
                        continuation.resume(parseResult(intent))
                    }
                }

                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    IntentFilter(resultAction),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )

                continuation.invokeOnCancellation {
                    if (handled.compareAndSet(false, true)) unregister(receiver)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    resultAction.hashCode(),
                    Intent(resultAction).setPackage(context.packageName),
                    pendingIntentFlags()
                )

                val intent = buildIntent(command, settings, background = true)
                intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent)

                val error = startTermuxService(intent)
                if (error != null && handled.compareAndSet(false, true)) {
                    unregister(receiver)
                    continuation.resume(ExecutionResult(error = error, exitCode = -1))
                }
            }
        }

        return result ?: ExecutionResult(
            error = "انتهت مهلة تنفيذ الأمر بعد $timeoutSeconds ثانية. " +
                "تأكد من تشغيل Termux ومن تفعيل allow-external-apps=true.",
            exitCode = -1
        )
    }

    private fun buildIntent(command: String, settings: AppSettings, background: Boolean): Intent {
        return Intent().apply {
            setClassName(settings.termuxPackageName, RUN_COMMAND_SERVICE)
            action = ACTION_RUN_COMMAND
            putExtra(EXTRA_COMMAND_PATH, "${settings.termuxPrefixPath}/bin/bash")
            putExtra(EXTRA_ARGUMENTS, arrayOf("-lc", command))
            putExtra(EXTRA_WORKDIR, settings.termuxHomePath)
            putExtra(EXTRA_BACKGROUND, background)
            putExtra(EXTRA_SESSION_ACTION, "0")
            putExtra(EXTRA_COMMAND_LABEL, "Linux Termux Panel")
        }
    }

    private fun startTermuxService(intent: Intent): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        null
    } catch (e: SecurityException) {
        Log.e(TAG, "Termux رفض الطلب", e)
        "رفض Termux تنفيذ الأمر. فعّل allow-external-apps=true في ~/.termux/termux.properties " +
            "وامنح التطبيق صلاحية com.termux.permission.RUN_COMMAND."
    } catch (e: Exception) {
        Log.e(TAG, "تعذّر تشغيل خدمة Termux", e)
        "تعذّر الاتصال بخدمة Termux: ${e.message ?: e.javaClass.simpleName}"
    }

    private fun unregister(receiver: BroadcastReceiver) {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // المستقبِل غير مسجّل، لا حاجة لفعل شيء
        }
    }

    private fun parseResult(intent: Intent?): ExecutionResult {
        val bundle = intent?.getBundleExtra(EXTRA_PLUGIN_RESULT_BUNDLE)
            ?: return ExecutionResult(error = "لم يُرجِع Termux أي نتيجة.", exitCode = -1)

        val stdout = bundle.getString(RESULT_STDOUT, "").orEmpty()
        val stderr = bundle.getString(RESULT_STDERR, "").orEmpty()
        val errmsg = bundle.getString(RESULT_ERRMSG, "").orEmpty()
        val exitCode = bundle.getInt(RESULT_EXIT_CODE, -1)
        val pluginError = bundle.getInt(RESULT_ERR, 0)

        val error = buildString {
            if (stderr.isNotBlank()) append(stderr.trim())
            if (pluginError != 0 && errmsg.isNotBlank()) {
                if (isNotEmpty()) append('\n')
                append(errmsg.trim())
            }
        }

        return ExecutionResult(
            output = stdout.trim(),
            error = error,
            exitCode = if (pluginError != 0) -1 else exitCode
        )
    }

    private fun pendingIntentFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

    companion object {
        private const val TAG = "TermuxRunCommand"

        private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
        private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
        private const val EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
        private const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
        private const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
        private const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
        private const val EXTRA_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"
        private const val EXTRA_COMMAND_LABEL = "com.termux.RUN_COMMAND_COMMAND_LABEL"
        private const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"
        private const val EXTRA_PLUGIN_RESULT_BUNDLE = "result"

        private const val RESULT_STDOUT = "stdout"
        private const val RESULT_STDERR = "stderr"
        private const val RESULT_EXIT_CODE = "exitCode"
        private const val RESULT_ERR = "err"
        private const val RESULT_ERRMSG = "errmsg"
    }
}
