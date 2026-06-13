package com.example.healthsync.frontend.utils;

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

class FoodLabelDetectorHelper(private val threshold: Float = 0.60f) {
    private val TAG = "FoodLabelDetector"
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(threshold)
            .build()
    )

    // List of generic labels to ignore
    private val genericLabels = setOf(
        "food", "meal", "dish", "cuisine", "ingredient", "tableware", 
        "produce", "recipe", "fast food", "comfort food", "vegetable", 
        "fruit", "junk food", "snack", "meat", "breakfast", "lunch", "dinner"
    )

    data class DetectionResult(val label: String, val confidence: Float)

    suspend fun detectSpecificFood(bitmap: Bitmap): DetectionResult? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labels = labeler.process(image).await()
            
            Log.d(TAG, "Raw Labels detected: ${labels.size}")
            
            // Filter out generic labels and sort by confidence
            val specificFood = labels
                .filter { label -> !genericLabels.contains(label.text.lowercase()) }
                .maxByOrNull { it.confidence }

            if (specificFood != null) {
                Log.d(TAG, "Specific Food Found: ${specificFood.text} (${specificFood.confidence})")
                DetectionResult(specificFood.text, specificFood.confidence)
            } else {
                Log.d(TAG, "Only generic labels found or no food detected.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Detection failed: ${e.message}")
            null
        }
    }
}
