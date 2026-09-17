package com.example.dowatch.data
import androidx.room.Entity
import androidx.room.PrimaryKey

class Show {
    @Entity
    data class movie(
        @PrimaryKey(autoGenerate = true)
        val id: Int=0
    )
}