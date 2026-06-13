package com.example.healthsync.backend.data.local

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
        SleepEntryEntity::class,
        StepEntity::class,
        JoinedChallengeEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun healthDao(): HealthDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun stepDao(): StepDao
    abstract fun challengeDao(): ChallengeDao

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
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
