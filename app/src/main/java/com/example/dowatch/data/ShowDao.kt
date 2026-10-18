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

    @Query("UPDATE Show SET status='Completed' WHERE id = :id")
    suspend fun complete(id: Int)

    @Query("UPDATE Show SET status='Plan To Watch' WHERE id = :id")
    suspend fun later(id: Int)

    @Query("UPDATE Show SET status='Dropped' WHERE id = :id")
    suspend fun dropped(id: Int)
}
