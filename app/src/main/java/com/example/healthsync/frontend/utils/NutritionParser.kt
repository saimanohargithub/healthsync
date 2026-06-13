package com.example.healthsync.frontend.utils;

import android.util.Log

class NutritionParser {
    private val TAG = "NutritionParser"

    data class ParsedNutrition(
        val calories: Double = 0.0,
        val protein: Double = 0.0,
        val carbs: Double = 0.0,
        val fat: Double = 0.0,
        val fiber: Double = 0.0,
        val sugar: Double = 0.0,
        val sodium: Double = 0.0,
        val calcium: Double = 0.0,
        val iron: Double = 0.0,
        val potassium: Double = 0.0
    )

    fun parse(text: String): ParsedNutrition {
        val lines = text.lowercase().lines()
        
        return ParsedNutrition(
            calories = extractValue(text, "calories", "energy", "kcal", "cal"),
            protein = extractValue(text, "protein", "proteins"),
            carbs = extractValue(text, "carbohydrate", "total carbohydrate", "carbs", "carb"),
            fat = extractValue(text, "total fat", "fat", "fats", "lipids"),
            fiber = extractValue(text, "dietary fiber", "fiber", "fibres"),
            sugar = extractValue(text, "sugars", "total sugars", "sugar"),
            sodium = extractValue(text, "sodium", "salt"),
            calcium = extractValue(text, "calcium"),
            iron = extractValue(text, "iron"),
            potassium = extractValue(text, "potassium")
        )
    }

    private fun extractValue(text: String, vararg keywords: String): Double {
        for (keyword in keywords) {
            // Regex to find keyword followed by numbers, handling spaces and units (g, mg, kcal)
            // Supports formats like "Protein 12g", "Fat: 5.5", "Calories 200 kcal"
            val pattern = "(?i)$keyword\\s*[:\\-]?\\s*(\\d+\\.?\\d*)\\s*(g|mg|kcal|cal)?"
            val regex = Regex(pattern)
            val match = regex.find(text)
            
            if (match != null) {
                val value = match.groupValues[1].toDoubleOrNull() ?: 0.0
                Log.d(TAG, "Extracted $keyword: $value")
                return value
            }
        }
        return 0.0
    }
}
