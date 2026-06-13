package com.example.healthsync.frontend.data.remote;

import com.google.gson.annotations.SerializedName

data class FoodSearchResponse(
    @SerializedName("foods") val foods: List<UsdaFood>
)

data class UsdaFood(
    @SerializedName("fdcId") val fdcId: Int,
    @SerializedName("description") val description: String,
    @SerializedName("foodNutrients") val foodNutrients: List<FoodNutrient>
)

data class FoodNutrient(
    @SerializedName("nutrientId") val nutrientId: Int,
    @SerializedName("nutrientName") val nutrientName: String,
    @SerializedName("unitName") val unitName: String,
    @SerializedName("value") val value: Double
)
