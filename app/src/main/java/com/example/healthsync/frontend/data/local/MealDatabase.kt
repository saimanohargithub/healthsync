package com.example.healthsync.frontend.data.local;

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MealEntity::class,
        WaterLog::class,
        SleepLog::class,
        StressLog::class,
        MoodLog::class,
        UserEntity::class,
        MealPlanEntity::class,
        SleepEntryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun healthDao(): HealthDao
    abstract fun mealPlanDao(): MealPlanDao

    companion object {
        @Volatile
        private var INSTANCE: MealDatabase? = null

        fun getDatabase(context: Context): MealDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MealDatabase::class.java,
                    "health_sync_db"
                )
                .fallbackToDestructiveMigration() // Reset for now during backend build
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
