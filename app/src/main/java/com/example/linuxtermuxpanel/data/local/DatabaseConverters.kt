package com.example.linuxtermuxpanel.data.local

import androidx.room.TypeConverter
import java.util.Date

class DatabaseConverters {
    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let(::Date)
}
