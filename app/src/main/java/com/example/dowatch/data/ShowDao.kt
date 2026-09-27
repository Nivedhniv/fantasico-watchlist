package com.example.dowatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ShowDao{
    @Insert
    suspend fun insertshow(show: Show)

    @Query("SELECT * FROM Show")
    suspend fun getAllShows(): List<Show>

    @Query("SELECT COUNT(*) FROM Show")
    suspend fun getShowCount(): Int
}
