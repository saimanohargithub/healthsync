package com.example.healthsync.backend.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_history")
data class StepEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val steps: Int,
    val timestamp: Long
)
