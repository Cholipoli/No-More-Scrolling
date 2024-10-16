package com.example.nomorescrolling.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "click_event_database")
data class ClickEvent(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val timestamp: Long,
)