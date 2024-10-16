package com.example.nomorescrolling.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ScrollSession::class], version = 1)
@TypeConverters(Converters::class) // Ajoute ceci pour utiliser le type converter
abstract class ScrollSessionDatabase : RoomDatabase() {

    abstract fun scrollSessionDao(): ScrollSessionDao

    companion object {
        @Volatile
        private var INSTANCE: ScrollSessionDatabase? = null

        fun getDatabase(context: Context): ScrollSessionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScrollSessionDatabase::class.java,
                    "scroll_session_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
