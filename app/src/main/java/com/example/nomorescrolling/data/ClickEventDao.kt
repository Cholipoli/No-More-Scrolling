package com.example.nomorescrolling.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClickEventDao {
    @Insert
    suspend fun insert(clickEvent: ClickEvent)

    @Query("SELECT * FROM click_event_database WHERE timestamp > :timestamp ORDER BY timestamp ASC")
    suspend fun getClicksAfterTimestamp(timestamp: Long): List<ClickEvent>

    @Query("DELETE FROM click_event_database")
    suspend fun deleteClicks()

}
