package com.example.healthsync.backend.utils

import com.example.healthsync.backend.data.remote.UsdaFood

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
        var cal = 0.0
        var pro = 0.0
        var car = 0.0
        var fat = 0.0
        var fib = 0.0
        var sug = 0.0
        var sod = 0.0
        var cac = 0.0
        var iro = 0.0
        var pot = 0.0

        for (food in foods) {
            food.foodNutrients.forEach { nutrient ->
                val value = nutrient.value
                when (nutrient.nutrientName.lowercase()) {
                    "energy" -> cal += value
                    "protein" -> pro += value
                    "carbohydrate, by difference" -> car += value
                    "total lipid (fat)" -> fat += value
                    "fiber, total dietary" -> fib += value
                    "sugars, total including nlea" -> sug += value
                    "sodium, na" -> sod += value
                    "calcium, ca" -> cac += value
                    "iron, fe" -> iro += value
                    "potassium, k" -> pot += value
                }
            }
        }
        return NutritionTotals(cal, pro, car, fat, fib, sug, sod, cac, iro, pot)
    }
}
