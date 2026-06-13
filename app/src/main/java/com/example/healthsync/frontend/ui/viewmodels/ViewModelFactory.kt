package com.example.healthsync.frontend.ui.viewmodels;

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthsync.frontend.data.local.MealDatabase
import com.example.healthsync.frontend.data.repository.HealthRepository
import com.example.healthsync.frontend.data.repository.MealRepository
import com.example.healthsync.frontend.utils.PreferenceManager

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    private val db = MealDatabase.getDatabase(context)
    private val prefs = PreferenceManager(context)
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HealthViewModel::class.java) -> {
                HealthViewModel(HealthRepository(db.healthDao(), prefs)) as T
            }
            modelClass.isAssignableFrom(MealScannerViewModel::class.java) -> {
                MealScannerViewModel(MealRepository(context, db.mealDao())) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
