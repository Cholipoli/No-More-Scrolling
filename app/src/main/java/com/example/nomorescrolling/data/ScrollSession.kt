package com.example.nomorescrolling.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scroll_events")
data class ScrollSession(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val numberOfScrolls: Int,
    val sessionDurationInMil: Long,
    val averageTimeBetweenScrolls: Float?,
    val isDumpScroll: Boolean = false,
    val appUsed: Map<String, Float>
)