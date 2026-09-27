package com.example.dowatch.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Show::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao
}