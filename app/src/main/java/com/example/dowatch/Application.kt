package com.example.dowatch

import android.app.Application
import androidx.room.Room
import com.example.dowatch.data.AppDatabase
import com.google.android.material.color.DynamicColors

class Application : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dowatch_database"
        ).build()
    }
}