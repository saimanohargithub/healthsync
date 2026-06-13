package com.example.healthsync.frontend.data.local;

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_entries")
data class SleepEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val dayOfWeek: String, // Monday, Tuesday, etc.
    val sleepHours: Int,
    val sleepMinutes: Int,
    val sleepScore: Int,
    val timestamp: Long
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long
)

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hours: Float,
    val score: Int,
    val timestamp: Long
)

@Entity(tableName = "stress_logs")
data class StressLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val level: String, // Low, Medium, High
    val timestamp: Long
)

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mood: String,
    val note: String,
    val timestamp: Long
)

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val age: Int,
    val gender: String,
    val heightCm: Float,
    val weightKg: Float,
    val points: Int,
    val lastSync: Long
)
