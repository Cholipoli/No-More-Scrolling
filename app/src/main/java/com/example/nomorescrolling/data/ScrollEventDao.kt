package com.example.nomorescrolling.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScrollEventDao {
    @Insert
    suspend fun insert(scrollEvent: ScrollEvent)

    @Query("SELECT * FROM scroll_events")
    suspend fun getAllScrollEvents(): List<ScrollEvent>

    @Query("SELECT * FROM scroll_events WHERE id = :id")
    suspend fun getScrollEventById(id: Int): ScrollEvent?

    @Query("SELECT * FROM scroll_events ORDER BY id DESC LIMIT 1")
    suspend fun getLastScrollEvent(): ScrollEvent?

    @Query("SELECT * FROM scroll_events ORDER BY id DESC LIMIT 1 OFFSET 1")
    suspend fun getAvantDernier(): ScrollEvent?

    @Query("SELECT * FROM scroll_events WHERE( isAScrollSessionStart = 1) ORDER BY id DESC LIMIT 1 OFFSET 1")
    suspend fun getLastSessionStart(): ScrollEvent?

    @Query("SELECT * FROM scroll_events WHERE id >= (SELECT id FROM scroll_events WHERE isAScrollSessionStart = 1 ORDER BY id DESC LIMIT 1 OFFSET 1) AND id < (SELECT id FROM scroll_events ORDER BY id DESC LIMIT 1)")
    suspend fun getLastSession(): List<ScrollEvent>

    @Query("SELECT * FROM (SELECT * FROM scroll_events WHERE id >= (SELECT id FROM scroll_events WHERE isAScrollSessionStart = 1 ORDER BY id DESC LIMIT 1) ORDER BY id DESC LIMIT 30) subquery ORDER BY id ASC;")
    suspend fun getScrollsSinceLastSessionStart(): List<ScrollEvent>

    @Query("DELETE FROM scroll_events")
    fun deleteScrolls()

    @Update
    suspend fun update(scrollEvent: ScrollEvent) // Add this method for updating a scroll event

}
