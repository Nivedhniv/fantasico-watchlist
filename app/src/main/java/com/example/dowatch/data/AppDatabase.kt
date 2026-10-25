package com.example.dowatch.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Show::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao
}