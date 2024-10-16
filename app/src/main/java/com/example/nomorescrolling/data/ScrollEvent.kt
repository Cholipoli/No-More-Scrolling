package com.example.nomorescrolling.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scroll_events")
data class ScrollEvent(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val timestamp: Long,
    var packageName: String = "Unknown",
    var isAScrollSessionStart: Boolean = false
)