package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import com.example.linuxtermuxpanel.data.model.Service
import com.example.linuxtermuxpanel.data.repository.ExecutionHistoryRepository
import com.example.linuxtermuxpanel.data.repository.ServiceRepository
import com.example.linuxtermuxpanel.execution.CommandExecutor
import com.example.linuxtermuxpanel.execution.ExecutionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** الإجراءات الممكنة على الخدمة. */
enum class ServiceAction(val labelAr: String) {
    START("تشغيل"),
    STOP("إيقاف"),
    STATUS("الحالة"),
    RESTART("إعادة تشغيل")
}

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val executionHistoryRepository: ExecutionHistoryRepository,
    private val commandExecutor: CommandExecutor
) : ViewModel() {

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _lastExecutionResult = MutableStateFlow<ExecutionResultUiState?>(null)
    val lastExecutionResult: StateFlow<ExecutionResultUiState?> = _lastExecutionResult.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            serviceRepository.getAllServices().collect { _services.value = it }
        }
    }

    fun addService(service: Service) = viewModelScope.launch {
        serviceRepository.insertService(service.copy(createdAt = Date(), updatedAt = Date()))
    }

    fun updateService(service: Service) = viewModelScope.launch {
        serviceRepository.updateService(service.copy(updatedAt = Date()))
    }

    fun deleteService(service: Service) = viewModelScope.launch {
        serviceRepository.deleteService(service)
    }

    fun deleteAllServices() = viewModelScope.launch { serviceRepository.deleteAllServices() }

    fun commandFor(service: Service, action: ServiceAction): String? = when (action) {
        ServiceAction.START -> service.startCommand
        ServiceAction.STOP -> service.stopCommand
        ServiceAction.STATUS -> service.statusCommand
        ServiceAction.RESTART -> service.restartCommand
    }?.takeIf { it.isNotBlank() }

    /** تنفيذ أحد أوامر الخدمة (تشغيل/إيقاف/حالة/إعادة تشغيل) وتسجيله في السجل. */
    fun runAction(service: Service, action: ServiceAction) {
        if (_isExecuting.value) return

        val command = commandFor(service, action)
        if (command == null) {
            _lastExecutionResult.value = ExecutionResultUiState(
                title = "${service.name} — ${action.labelAr}",
                success = false,
                output = "",
                error = "لا يوجد أمر مُعرّف لهذا الإجراء.",
                exitCode = -1
            )
            return
        }

        viewModelScope.launch {
            _isExecuting.value = true
            val startedAt = Date()

            val result = try {
                commandExecutor.execute(command = command, environment = service.environment)
            } catch (e: Exception) {
                ExecutionResult(error = e.message ?: "خطأ غير معروف", exitCode = -1)
            }

            executionHistoryRepository.insertExecutionHistory(
                ExecutionHistory(
                    commandId = null,
                    label = "${service.name} — ${action.labelAr}",
                    commandText = command,
                    output = result.output,
                    error = result.error,
                    exitCode = result.exitCode,
                    startedAt = startedAt,
                    finishedAt = Date(),
                    success = result.success
                )
            )

            _lastExecutionResult.value = ExecutionResultUiState(
                title = "${service.name} — ${action.labelAr}",
                success = result.success,
                output = result.output,
                error = result.error,
                exitCode = result.exitCode
            )
            _isExecuting.value = false
        }
    }

    fun clearExecutionResult() {
        _lastExecutionResult.value = null
    }
}
