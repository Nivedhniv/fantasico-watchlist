package com.example.dowatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import retrofit2.http.DELETE


@Dao
interface ShowDao{
    @Insert
    suspend fun insertshow(show: Show)

    @Query("SELECT * FROM Show")
    suspend fun getAllShows(): List<Show>

    @Query("SELECT * FROM Show WHERE id = :id")
    suspend fun getOne(id: Int): Show?

    @Query("SELECT COUNT(*) FROM Show")
    suspend fun getShowCount(): Int

    @Query("DELETE FROM Show WHERE id = :id")
    suspend fun delete(id: Int)
}
