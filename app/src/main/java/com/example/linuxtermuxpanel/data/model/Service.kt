package com.example.linuxtermuxpanel.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startCommand: String,
    val stopCommand: String? = null,
    val statusCommand: String? = null,
    val restartCommand: String? = null,
    val environment: String, // "Termux" or "Ubuntu"
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)