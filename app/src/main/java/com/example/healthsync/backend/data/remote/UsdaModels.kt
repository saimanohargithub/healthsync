package com.example.healthsync.backend.data.remote

data class UsdaSearchResponse(
    val foods: List<UsdaFood>
)

data class UsdaFood(
    val description: String,
    val foodNutrients: List<UsdaNutrient>
)

data class UsdaNutrient(
    val nutrientName: String,
    val value: Double,
    val unitName: String
)
