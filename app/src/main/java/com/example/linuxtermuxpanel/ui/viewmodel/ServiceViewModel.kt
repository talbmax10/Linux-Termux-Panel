package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.model.Service
import com.example.linuxtermuxpanel.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {
    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    init { loadServices() }

    private fun loadServices() {
        viewModelScope.launch {
            serviceRepository.getAllServices().collect { _services.value = it }
        }
    }

    fun addService(service: Service) = viewModelScope.launch { serviceRepository.insertService(service) }
    fun updateService(service: Service) = viewModelScope.launch { serviceRepository.updateService(service) }
    fun deleteService(service: Service) = viewModelScope.launch { serviceRepository.deleteService(service) }
    fun deleteAllServices() = viewModelScope.launch { serviceRepository.deleteAllServices() }
}
