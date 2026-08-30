package com.example.linuxtermuxpanel.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.linuxtermuxpanel.data.model.Command

@Database(
    entities = [Command::class, Service::class, ExecutionHistory::class],
    version = 1,
    exportSchema = false
)
abstract class CommandDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun serviceDao(): ServiceDao
    abstract fun executionHistoryDao(): ExecutionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: CommandDatabase? = null

        fun getDatabase(context: Context): CommandDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CommandDatabase::class.java,
                    "command_database"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
