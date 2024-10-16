package com.example.nomorescrolling.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScrollSessionDao {
    @Insert
    suspend fun insert(scrollSession: ScrollSession)

}
