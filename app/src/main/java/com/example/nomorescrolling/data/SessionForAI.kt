package com.example.nomorescrolling.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_for_ai_events")
data class SessionForAI(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val averageScrollFrequecy: Float,
    val variationScore: Float,
    val timeBetweenClicks : Float,
    val dominantApp : String,
    val clickScrollRatio : Float,
    val typeScrollRatio : Float,
    var isDumpScroll: Boolean = false,

    )