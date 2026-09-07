package com.example.linuxtermuxpanel.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.linuxtermuxpanel.data.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * تخزين واسترجاع إعدادات التطبيق باستخدام DataStore.
 * كانت الإعدادات سابقًا في الذاكرة فقط ولا تُحفظ ولا تُستخدم أثناء التنفيذ.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val TERMUX_PACKAGE = stringPreferencesKey("termux_package_name")
        val UBUNTU_LOGIN = stringPreferencesKey("ubuntu_login_command")
        val AUTO_WRAP = booleanPreferencesKey("auto_wrap_ubuntu_commands")
        val TIMEOUT = intPreferencesKey("timeout_seconds")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { preferences ->
            val defaults = AppSettings()
            AppSettings(
                termuxPackageName = preferences[Keys.TERMUX_PACKAGE]
                    ?.takeIf { it.isNotBlank() } ?: defaults.termuxPackageName,
                ubuntuLoginCommand = preferences[Keys.UBUNTU_LOGIN]
                    ?.takeIf { it.isNotBlank() } ?: defaults.ubuntuLoginCommand,
                autoWrapUbuntuCommands = preferences[Keys.AUTO_WRAP] ?: defaults.autoWrapUbuntuCommands,
                timeoutSeconds = preferences[Keys.TIMEOUT]?.takeIf { it > 0 } ?: defaults.timeoutSeconds
            )
        }

    suspend fun currentSettings(): AppSettings = settings.first()

    suspend fun update(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.TERMUX_PACKAGE] = settings.termuxPackageName.trim()
                .ifBlank { AppSettings.DEFAULT_TERMUX_PACKAGE }
            preferences[Keys.UBUNTU_LOGIN] = settings.ubuntuLoginCommand.trim()
                .ifBlank { AppSettings.DEFAULT_UBUNTU_LOGIN }
            preferences[Keys.AUTO_WRAP] = settings.autoWrapUbuntuCommands
            preferences[Keys.TIMEOUT] = settings.timeoutSeconds.coerceIn(1, 3600)
        }
    }
}
