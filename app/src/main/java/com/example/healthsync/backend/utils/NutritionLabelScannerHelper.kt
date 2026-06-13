package com.example.healthsync.backend.utils

import android.graphics.Bitmap
import com.example.healthsync.backend.data.local.MealEntity

class NutritionLabelScannerHelper {
    data class ScanResult(val meal: MealEntity, val rawText: String)

    fun scanLabel(bitmap: Bitmap, imagePath: String): ScanResult? {
        return null
    }
}
