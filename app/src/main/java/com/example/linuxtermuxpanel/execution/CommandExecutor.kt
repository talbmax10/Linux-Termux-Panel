package com.example.linuxtermuxpanel.execution

import com.example.linuxtermuxpanel.data.model.AppSettings
import com.example.linuxtermuxpanel.data.model.Environments
import com.example.linuxtermuxpanel.data.preferences.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * نقطة الدخول الموحّدة لتنفيذ الأوامر.
 *
 * يقرأ الإعدادات المحفوظة (اسم حزمة Termux، أمر الدخول إلى Ubuntu، المهلة)،
 * يلفّ أوامر Ubuntu عند الحاجة، ثم ينفّذها عبر Termux أو عبر شِل أندرويد كخطة بديلة.
 */
@Singleton
class CommandExecutor @Inject constructor(
    private val termuxExecutor: TermuxRunCommandExecutor,
    private val shellExecutor: ShellCommandExecutor,
    private val settingsRepository: SettingsRepository
) {

    suspend fun execute(
        command: String,
        environment: String,
        interactive: Boolean = false
    ): ExecutionResult {
        if (command.isBlank()) {
            return ExecutionResult(error = "الأمر فارغ", exitCode = -1)
        }

        val settings = settingsRepository.currentSettings()
        val finalCommand = wrapForEnvironment(command, environment, settings)

        return if (termuxExecutor.isTermuxInstalled(settings.termuxPackageName)) {
            termuxExecutor.execute(finalCommand, settings, interactive)
        } else {
            shellExecutor.execute(finalCommand, settings.timeoutSeconds.toLong())
        }
    }

    fun wrapForEnvironment(command: String, environment: String, settings: AppSettings): String =
        if (environment.equals(Environments.UBUNTU, ignoreCase = true) && settings.autoWrapUbuntuCommands) {
            UbuntuCommandWrapper(settings.ubuntuLoginCommand).wrap(command)
        } else {
            command
        }
}
