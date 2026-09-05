package com.example.linuxtermuxpanel.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.linuxtermuxpanel.data.model.Command
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {

    @Query("SELECT * FROM commands ORDER BY name ASC")
    fun getAllCommands(): Flow<List<Command>>

    @Query("SELECT * FROM commands WHERE id = :commandId")
    fun getCommandById(commandId: Long): Command

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: Command): Long

    @Update
    suspend fun updateCommand(command: Command)

    @Delete
    suspend fun deleteCommand(command: Command)

    @Query("DELETE FROM commands")
    suspend fun deleteAllCommands()
}