package com.example.nomorescrolling.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keyboard_event_database")
data class KeyboardEvent(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val timestamp: Long,
)