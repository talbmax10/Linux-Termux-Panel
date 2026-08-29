package com.example.linuxtermuxpanel.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "commands")
data class Command(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val command: String,
    val environment: String, // "Termux" or "Ubuntu"
    val icon: String? = null, // Could be a URL or resource name, for simplicity we store as string
    val isFavorite: Boolean = false,
    val runInBackground: Boolean = false,
    val needsInteractiveTerminal: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)