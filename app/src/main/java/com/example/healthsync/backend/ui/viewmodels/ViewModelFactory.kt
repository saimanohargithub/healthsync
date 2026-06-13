package com.example.healthsync.backend.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthsync.backend.data.local.MealDatabase
import com.example.healthsync.backend.data.repository.HealthRepository
import com.example.healthsync.backend.data.repository.MealRepository
import com.example.healthsync.frontend.utils.PreferenceManager

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = MealDatabase.getDatabase(context)
        val prefs = PreferenceManager(context)
        
        return when {
            modelClass.isAssignableFrom(HealthViewModel::class.java) -> {
                HealthViewModel(HealthRepository(database.healthDao(), prefs)) as T
            }
            modelClass.isAssignableFrom(MealScannerViewModel::class.java) -> {
                MealScannerViewModel(MealRepository(context, database.mealDao())) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(HealthRepository(database.healthDao(), prefs)) as T
            }
            modelClass.isAssignableFrom(HealthDataViewModel::class.java) -> {
                HealthDataViewModel(
                    HealthRepository(database.healthDao(), prefs),
                    MealRepository(context, database.mealDao())
                ) as T
            }
            modelClass.isAssignableFrom(CommunityViewModel::class.java) -> {
                CommunityViewModel(
                    com.example.healthsync.backend.data.repository.CommunityRepository(),
                    HealthRepository(database.healthDao(), prefs),
                    com.example.healthsync.backend.data.repository.ChallengeRepository(database.challengeDao()),
                    com.example.healthsync.backend.data.repository.LeaderboardRepository()
                ) as T
            }
            modelClass.isAssignableFrom(StepViewModel::class.java) -> {
                StepViewModel(
                    com.example.healthsync.backend.data.repository.StepRepository(database.stepDao(), prefs)
                ) as T
            }
            modelClass.isAssignableFrom(LeaderboardViewModel::class.java) -> {
                LeaderboardViewModel(
                    com.example.healthsync.backend.data.repository.LeaderboardRepository()
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
