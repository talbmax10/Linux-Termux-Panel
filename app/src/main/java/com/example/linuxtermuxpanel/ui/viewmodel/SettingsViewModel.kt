package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.ui.settings.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
) : ViewModel() {

    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // Load from DataStore or SharedPreferences, for now we'll use a default.
        // In a real app, we would use DataStore.
        viewModelScope.launch {
            // For simplicity, we'll just set a default.
            _settings.value = Settings.getDefault()
        }
    }

    fun saveSettings(settings: Settings) = viewModelScope.launch {
        // Save to DataStore or SharedPreferences.
        // For now, we just update the state.
        _settings.value = settings
    }
}

// Default settings
data class Settings(
    val termuxPackageName: String = "com.termux",
    val ubuntuLoginCommand: String = "proot-distro login ubuntu",
    val ubuntuDistributionName: String = "ubuntu",
    val autoWrapUbuntuCommands: Boolean = true,
    val timeoutSeconds: Int = 30
) {
    companion object {
        fun getDefault(): Settings = Settings()
    }
}
