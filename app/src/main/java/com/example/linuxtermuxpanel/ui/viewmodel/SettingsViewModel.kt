package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.model.AppSettings
import com.example.linuxtermuxpanel.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            _settings.value = settingsRepository.currentSettings()
        }
    }

    fun onTermuxPackageChanged(value: String) {
        _settings.value = _settings.value.copy(termuxPackageName = value)
    }

    fun onUbuntuLoginCommandChanged(value: String) {
        _settings.value = _settings.value.copy(ubuntuLoginCommand = value)
    }

    fun onAutoWrapChanged(value: Boolean) {
        _settings.value = _settings.value.copy(autoWrapUbuntuCommands = value)
    }

    fun onTimeoutChanged(value: Int) {
        _settings.value = _settings.value.copy(timeoutSeconds = value)
    }

    fun save() {
        viewModelScope.launch {
            val current = _settings.value
            settingsRepository.update(current)
            // إعادة القراءة لضمان عرض القيم بعد التصحيح (قصّ المسافات والحدود)
            _settings.value = settingsRepository.currentSettings()
            _message.value = "تم حفظ الإعدادات"
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.update(AppSettings())
            _settings.value = settingsRepository.currentSettings()
            _message.value = "تمت استعادة الإعدادات الافتراضية"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
