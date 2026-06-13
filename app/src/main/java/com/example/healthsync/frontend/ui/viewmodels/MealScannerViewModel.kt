package com.example.healthsync.frontend.ui.viewmodels;

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsync.frontend.data.local.MealEntity
import com.example.healthsync.frontend.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MealScannerUiState {
    object Idle : MealScannerUiState()
    object Loading : MealScannerUiState()
    data class Success(val result: MealRepository.AnalysisResult) : MealScannerUiState()
    data class Error(val message: String) : MealScannerUiState()
}

class MealScannerViewModel(private val repository: MealRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MealScannerUiState>(MealScannerUiState.Idle)
    val uiState: StateFlow<MealScannerUiState> = _uiState.asStateFlow()

    fun analyzeFood(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = MealScannerUiState.Loading
            repository.analyzeFoodImage(bitmap)
                .onSuccess { result ->
                    _uiState.value = MealScannerUiState.Success(result)
                }
                .onFailure { error ->
                    _uiState.value = MealScannerUiState.Error(error.message ?: "Identification failed")
                }
        }
    }

    fun analyzeLabel(bitmap: Bitmap, imagePath: String) {
        viewModelScope.launch {
            _uiState.value = MealScannerUiState.Loading
            repository.analyzeNutritionLabel(bitmap, imagePath)
                .onSuccess { result ->
                    _uiState.value = MealScannerUiState.Success(result)
                }
                .onFailure { error ->
                    _uiState.value = MealScannerUiState.Error(error.message ?: "OCR failed")
                }
        }
    }

    fun saveMeal(meal: MealEntity) {
        viewModelScope.launch {
            repository.saveMeal(meal)
            _uiState.value = MealScannerUiState.Idle
        }
    }

    fun reset() {
        _uiState.value = MealScannerUiState.Idle
    }
}
