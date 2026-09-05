package com.example.linuxtermuxpanel.di

import com.example.linuxtermuxpanel.execution.ShellCommandExecutor
import com.example.linuxtermuxpanel.execution.TermuxCommandExecutor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExecutionModule {

    @Binds
    @Singleton
    abstract fun bindTermuxCommandExecutor(
        shellCommandExecutor: ShellCommandExecutor
    ): TermuxCommandExecutor
}
