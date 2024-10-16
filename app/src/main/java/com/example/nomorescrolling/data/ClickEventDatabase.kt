package com.example.nomorescrolling.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ClickEvent::class], version = 1)
@TypeConverters(Converters::class) // Ajoute ceci pour utiliser le type converter
abstract class ClickEventDatabase : RoomDatabase() {

    abstract fun clickEventDao(): ClickEventDao

    companion object {
        @Volatile
        private var INSTANCE: ClickEventDatabase? = null

        fun getDatabase(context: Context): ClickEventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClickEventDatabase::class.java,
                    "click_event_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
