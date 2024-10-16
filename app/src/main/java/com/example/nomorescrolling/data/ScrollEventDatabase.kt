package com.example.nomorescrolling.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScrollEvent::class], version = 1)
abstract class ScrollEventDatabase : RoomDatabase() {

    abstract fun scrollEventDao(): ScrollEventDao

    companion object {
        @Volatile
        private var INSTANCE: ScrollEventDatabase? = null

        fun getDatabase(context: Context): ScrollEventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScrollEventDatabase::class.java,
                    "scroll_event_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
