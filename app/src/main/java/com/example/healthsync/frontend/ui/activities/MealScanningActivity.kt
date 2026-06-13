package com.example.healthsync.frontend.ui.activities;

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthsync.frontend.data.local.MealDatabase
import com.example.healthsync.frontend.data.repository.MealRepository
import com.example.healthsync.frontend.ui.screens.MealScannerScreen
import com.example.healthsync.frontend.ui.viewmodels.MealScannerViewModel

class MealScanningActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imagePath = intent.getStringExtra("image_path") ?: ""
        if (imagePath.isEmpty()) {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val database = MealDatabase.getDatabase(applicationContext)
        val repository = MealRepository(applicationContext, database.mealDao())
        
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MealScannerViewModel(repository) as T
            }
        })[MealScannerViewModel::class.java]

        setContent {
            MealScannerScreen(
                viewModel = viewModel,
                imagePath = imagePath,
                onBack = { finish() },
                onSaved = {
                    Toast.makeText(this, "Meal saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        }
    }
}
