package com.example.dowatch.data
import androidx.room.Entity
import androidx.room.PrimaryKey

    @Entity
    data class Show(
        @PrimaryKey(autoGenerate = true)
        val id: Int=0,
        val externalId: String,
        val title: String,
        val posterurl:String,
        val status: String,
        val year: String,
        val myrating: Float? = null,
        val date: String? = null,
        val notes: String? = null
    )
