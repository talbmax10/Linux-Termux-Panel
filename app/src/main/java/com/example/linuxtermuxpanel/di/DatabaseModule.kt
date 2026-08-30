package com.example.linuxtermuxpanel.di

import android.content.Context
import com.example.linuxtermuxpanel.data.local.CommandDao
import com.example.linuxtermuxpanel.data.local.CommandDatabase
import com.example.linuxtermuxpanel.data.local.ExecutionHistoryDao
import com.example.linuxtermuxpanel.data.local.ServiceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CommandDatabase =
        CommandDatabase.getDatabase(context)

    @Provides
    fun provideCommandDao(database: CommandDatabase): CommandDao = database.commandDao()

    @Provides
    fun provideServiceDao(database: CommandDatabase): ServiceDao = database.serviceDao()

    @Provides
    fun provideExecutionHistoryDao(database: CommandDatabase): ExecutionHistoryDao =
        database.executionHistoryDao()
}
