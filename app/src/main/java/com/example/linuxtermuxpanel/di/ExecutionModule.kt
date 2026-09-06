package com.example.linuxtermuxpanel.di

import android.content.Context
import com.example.linuxtermuxpanel.execution.FileBasedTermuxExecutor
import com.example.linuxtermuxpanel.execution.TermuxCommandExecutor
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
        return FileBasedTermuxExecutor(context, "com.termux", 30L)
    }
}
