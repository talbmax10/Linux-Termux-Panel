package com.example.linuxtermuxpanel.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "execution_history",
    foreignKeys = [
        ForeignKey(
            entity = Command::class,
            parentColumns = ["id"],
            childColumns = ["commandId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("commandId")]
)
data class ExecutionHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commandId: Long,
    val commandText: String, // The actual command that was executed (including environment wrapper if needed)
    val output: String? = null,
    val error: String? = null,
    val exitCode: Int = -1, // -1 indicates not yet completed or error in execution
    val startedAt: Date = Date(),
    val finishedAt: Date? = null,
    val success: Boolean = false
)