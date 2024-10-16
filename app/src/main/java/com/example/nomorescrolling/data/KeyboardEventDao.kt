package com.example.nomorescrolling.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface KeyboardEventDao {
    @Insert
    suspend fun insert(keyboardEvent: KeyboardEvent)

    @Query("SELECT * FROM keyboard_event_database WHERE timestamp > :timestamp ORDER BY timestamp ASC")
    suspend fun getTypesAfterTimestamp(timestamp: Long): List<KeyboardEvent>

    @Query("DELETE FROM keyboard_event_database")
    suspend fun deleteTypes()

    @Query("SELECT * FROM keyboard_event_database")
    suspend fun getKeyboardEvents(): List<KeyboardEvent>

}
