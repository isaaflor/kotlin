package com.example.calculadoradeimc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BMIRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bmiDao(): BMIDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bmi_database"
                )
                    .fallbackToDestructiveMigration() // Wipes data if schema changes (Good for dev)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}