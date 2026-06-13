package com.example.healthsync.frontend.data.repository;

import android.content.Context
import android.graphics.Bitmap
import com.example.healthsync.BuildConfig
import com.example.healthsync.frontend.data.local.MealDao
import com.example.healthsync.frontend.data.local.MealEntity
import com.example.healthsync.frontend.data.remote.UsdaApiService
import com.example.healthsync.frontend.utils.FoodLabelDetectorHelper
import com.example.healthsync.frontend.utils.NutritionLabelScannerHelper
import com.example.healthsync.frontend.utils.NutritionCalculator
import com.example.healthsync.frontend.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MealRepository(
    private val context: Context,
    private val mealDao: MealDao
) {
    private val foodDetector = FoodLabelDetectorHelper()
    private val labelScanner = NutritionLabelScannerHelper()
    private val nutritionCalculator = NutritionCalculator()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prefs = PreferenceManager(context)

    private val api = Retrofit.Builder()
        .baseUrl("https://api.nal.usda.gov/fdc/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UsdaApiService::class.java)

    data class AnalysisResult(
        val meal: MealEntity,
        val source: String, // "USDA" or "OCR"
        val confidence: Float = 0f,
        val rawOcrText: String? = null
    )

    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val detection = foodDetector.detectSpecificFood(bitmap)
            if (detection == null) {
                return@withContext Result.failure(Exception("Food could not be identified specifically. Please try another image."))
            }

            val response = api.searchFood(detection.label, 1, BuildConfig.USDA_API_KEY)
            val topFood = response.foods.firstOrNull() ?: return@withContext Result.failure(Exception("Nutrition data not found for specific food: ${detection.label}"))

            val totals = nutritionCalculator.calculateTotals(listOf(topFood))
            val meal = MealEntity(
                mealName = detection.label,
                imagePath = "",
                timestamp = System.currentTimeMillis(),
                calories = totals.calories,
                protein = totals.protein,
                carbs = totals.carbs,
                fat = totals.fat,
                fiber = totals.fiber,
                sugar = totals.sugar,
                sodium = totals.sodium,
                calcium = totals.calcium,
                iron = totals.iron,
                potassium = totals.potassium,
                scanType = "FOOD"
            )

            Result.success(AnalysisResult(meal, "USDA", detection.confidence))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeNutritionLabel(bitmap: Bitmap, imagePath: String): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val scanResult = labelScanner.scanLabel(bitmap, imagePath)
            if (scanResult == null) {
                return@withContext Result.failure(Exception("Nutrition label could not be read clearly. Please ensure it is well-lit and in focus."))
            }
            Result.success(AnalysisResult(scanResult.meal, "OCR", rawOcrText = scanResult.rawText))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveMeal(meal: MealEntity) = withContext(Dispatchers.IO) {
        mealDao.insertMeal(meal)
        updatePreferences(meal)
        syncToFirestore(meal)
        
        // Sync Social/Challenges
        val userId = auth.currentUser?.uid
        if (userId != null) {
            com.example.healthsync.frontend.firebase.FirestoreManager.syncChallengeProgress(userId, "diet", prefs.getTodayCalories());
        }
    }

    private fun updatePreferences(meal: MealEntity) {
        prefs.setTodayCalories(prefs.getTodayCalories() + meal.calories.toInt())
        prefs.setTodayProtein(prefs.getTodayProtein() + meal.protein.toFloat())
        prefs.setTodayCarbs(prefs.getTodayCarbs() + meal.carbs.toFloat())
        prefs.setTodayFat(prefs.getTodayFat() + meal.fat.toFloat())
        prefs.setTodaySodium(prefs.getTodaySodium() + meal.sodium.toFloat())
        
        calculateAndSetHealthScore()
    }

    private fun calculateAndSetHealthScore() {
        val calories = prefs.getTodayCalories()
        val protein = prefs.getTodayProtein()
        var score = 100
        if (calories > 2500) score -= 10
        if (protein < 50) score -= 5
        prefs.setHealthScore(score.coerceIn(0, 100))
    }

    private fun syncToFirestore(meal: MealEntity) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("meals").add(meal)
    }

    fun getTodayMeals(): Flow<List<MealEntity>> {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        return mealDao.getMealsToday(calendar.timeInMillis)
    }
}
