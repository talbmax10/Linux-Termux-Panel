package com.example.linuxtermuxpanel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linuxtermuxpanel.data.repository.ServiceRepository
import com.example.linuxtermuxpanel.data.model.Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            val services = serviceRepository.getAllServices()
            _services.value = services
        }
    }

    fun addService(service: Service) = viewModelScope.launch {
        serviceRepository.insertService(service)
        loadServices()
    }

    fun updateService(service: Service) = viewModelScope.launch {
        serviceRepository.updateService(service)
        loadServices()
    }

    fun deleteService(service: Service) = viewModelScope.launch {
        serviceRepository.deleteService(service)
        loadServices()
    }

    fun deleteAllServices() = viewModelScope.launch {
        serviceRepository.deleteAllServices()
        loadServices()
    }
}