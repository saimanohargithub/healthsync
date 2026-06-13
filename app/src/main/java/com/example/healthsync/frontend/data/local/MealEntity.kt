package com.example.healthsync.frontend.data.local;

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealName: String,
    val imagePath: String,
    val timestamp: Long,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
    val sodium: Double,
    val calcium: Double,
    val iron: Double,
    val potassium: Double,
    val scanType: String // "FOOD" or "LABEL"
)
