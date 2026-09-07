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
    // يكون null عندما يكون مصدر التنفيذ خدمة وليس أمرًا محفوظًا،
    // وبهذا لا نكسر قيد المفتاح الأجنبي (Foreign Key) عند تسجيل تنفيذ الخدمات.
    val commandId: Long? = null,
    val label: String? = null, // اسم الأمر/الخدمة كما يظهر للمستخدم
    val commandText: String, // الأمر الفعلي الذي نُفِّذ
    val output: String? = null,
    val error: String? = null,
    val exitCode: Int = -1, // -1 indicates not yet completed or error in execution
    val startedAt: Date = Date(),
    val finishedAt: Date? = null,
    val success: Boolean = false
)