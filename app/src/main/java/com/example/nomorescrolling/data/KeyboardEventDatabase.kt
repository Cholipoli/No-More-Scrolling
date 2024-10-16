package com.example.nomorescrolling.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [KeyboardEvent::class], version = 1)
@TypeConverters(Converters::class) // Ajoute ceci pour utiliser le type converter
abstract class KeyboardEventDatabase : RoomDatabase() {

    abstract fun keyboardEventDao(): KeyboardEventDao

    companion object {
        @Volatile
        private var INSTANCE: KeyboardEventDatabase? = null

        fun getDatabase(context: Context): KeyboardEventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyboardEventDatabase::class.java,
                    "keyboard_event_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
