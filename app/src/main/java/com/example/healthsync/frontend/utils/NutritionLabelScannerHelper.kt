package com.example.healthsync.frontend.utils;

import android.graphics.Bitmap
import android.util.Log
import com.example.healthsync.frontend.data.local.MealEntity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class NutritionLabelScannerHelper {
    private val TAG = "NutritionScanner"
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val parser = NutritionParser()

    data class ScanResult(val meal: MealEntity, val rawText: String)

    suspend fun scanLabel(bitmap: Bitmap, imagePath: String): ScanResult? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText: Text = recognizer.process(image).await()
            val fullText = visionText.text
            
            if (fullText.isBlank()) {
                Log.d(TAG, "No text detected in image.")
                return null
            }

            val parsed = parser.parse(fullText)

            val meal = MealEntity(
                mealName = "Packaged Food",
                imagePath = imagePath,
                timestamp = System.currentTimeMillis(),
                calories = parsed.calories,
                protein = parsed.protein,
                carbs = parsed.carbs,
                fat = parsed.fat,
                fiber = parsed.fiber,
                sugar = parsed.sugar,
                sodium = parsed.sodium,
                calcium = parsed.calcium,
                iron = parsed.iron,
                potassium = parsed.potassium,
                scanType = "LABEL"
            )
            
            ScanResult(meal, fullText)
        } catch (e: Exception) {
            Log.e(TAG, "OCR failed: ${e.message}")
            null
        }
    }
}
