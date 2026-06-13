package com.example.healthsync.backend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.healthsync.backend.data.repository.HealthRepository
import com.example.healthsync.backend.data.repository.MealRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class HealthDataViewModel(
    private val healthRepository: HealthRepository,
    private val mealRepository: MealRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    val userProfile = healthRepository.getUserProfile().asLiveData()
    val userEmail = flow { emit(auth.currentUser?.email ?: "No Email") }.asLiveData()

    val bmi = healthRepository.getUserProfile().map { user ->
        if (user == null || user.heightCm <= 0) 0f
        else {
            val heightM = user.heightCm / 100f
            user.weightKg / (heightM * heightM)
        }
    }.asLiveData()

    val waterIntake = healthRepository.getTodayWater().asLiveData()
    val sleepLogs = healthRepository.getWeeklySleepEntries().asLiveData()
    val moodHistory = healthRepository.getMoodHistory().asLiveData()
    val mealHistory = mealRepository.getAllMeals().asLiveData()
}
