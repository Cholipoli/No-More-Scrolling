// Converters.kt
package com.example.nomorescrolling.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromMap(map: Map<String, Float>?): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toMap(mapString: String?): Map<String, Float>? {
        return Gson().fromJson(mapString, object : TypeToken<Map<String, Float>>() {}.type)
    }
}
