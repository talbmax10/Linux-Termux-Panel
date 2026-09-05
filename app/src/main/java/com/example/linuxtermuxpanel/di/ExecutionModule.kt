package com.example.linuxtermuxpanel.di

import android.content.Context
import com.example.linuxtermuxpanel.execution.CommandExecutor
import com.example.linuxtermuxpanel.execution.FileBasedTermuxExecutor
import com.example.linuxtermuxpanel.execution.TermuxCommandExecutor
import com.example.linuxtermuxpanel.ui.viewmodel.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExecutionModule {

    @Provides
    @Singleton
    fun provideTermuxCommandExecutor(
        @ApplicationContext context: Context
    ): TermuxCommandExecutor {
        // Default timeout of 30 seconds
        return FileBasedTermuxExecutor(context, "com.termux", 30L)
    }

    @Provides
    @Singleton
    fun provideCommandExecutor(
        @ApplicationContext context: Context
    ): CommandExecutor {
        // Use default settings for now - in a real app you'd want to load from DataStore
        val settings = Settings()
        return CommandExecutor(context, settings)
    }
}
