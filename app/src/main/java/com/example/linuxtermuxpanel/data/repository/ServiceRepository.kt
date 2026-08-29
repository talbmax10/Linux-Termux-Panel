package com.example.linuxtermuxpanel.data.repository

import com.example.linuxtermuxpanel.data.local.ServiceDao
import com.example.linuxtermuxpanel.data.local.CommandDatabase
import com.example.linuxtermuxpanel.data.model.Service
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepository @Inject constructor(
    private val serviceDao: ServiceDao
) {
    suspend fun getAllServices(): List<Service> = serviceDao.getAllServices()

    suspend fun getServiceById(id: Long): Service = serviceDao.getServiceById(id)

    suspend fun insertService(service: Service): Long = serviceDao.insertService(service)

    suspend fun updateService(service: Service) = serviceDao.updateService(service)

    suspend fun deleteService(service: Service) = serviceDao.deleteService(service)

    suspend fun deleteAllServices() = serviceDao.deleteAllServices()
}