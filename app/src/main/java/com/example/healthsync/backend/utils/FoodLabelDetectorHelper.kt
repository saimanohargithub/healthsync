package com.example.healthsync.backend.utils

import android.graphics.Bitmap

class FoodLabelDetectorHelper {
    data class Detection(val label: String, val confidence: Float)

    fun detectSpecificFood(bitmap: Bitmap): Detection? {
        return Detection("Apple", 0.95f)
    }
}
