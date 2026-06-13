package com.example.healthsync.frontend.utils;

import com.example.healthsync.frontend.data.remote.UsdaFood

class NutritionCalculator {

    data class NutritionTotals(
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

    fun calculateTotals(foods: List<UsdaFood>): NutritionTotals {
        var calories = 0.0
        var protein = 0.0
        var carbs = 0.0
        var fat = 0.0
        var fiber = 0.0
        var sugar = 0.0
        var sodium = 0.0
        var calcium = 0.0
        var iron = 0.0
        var potassium = 0.0

        for (food in foods) {
            for (nutrient in food.foodNutrients) {
                when (nutrient.nutrientId) {
                    1008, 2047 -> calories += nutrient.value // Energy (kcal)
                    1003 -> protein += nutrient.value // Protein
                    1005 -> carbs += nutrient.value // Carbohydrates
                    1004 -> fat += nutrient.value // Total lipid (fat)
                    1079 -> fiber += nutrient.value // Fiber
                    2000 -> sugar += nutrient.value // Sugars
                    1093 -> sodium += nutrient.value // Sodium
                    1087 -> calcium += nutrient.value // Calcium
                    1089 -> iron += nutrient.value // Iron
                    1092 -> potassium += nutrient.value // Potassium
                }
            }
        }

        return NutritionTotals(
            calories, protein, carbs, fat, fiber, sugar, sodium, calcium, iron, potassium
        )
    }
}
