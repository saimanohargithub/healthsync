package com.example.healthsync.backend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthsync.backend.data.repository.StepRepository
import kotlinx.coroutines.launch

class StepViewModel(private val repository: StepRepository) : ViewModel() {

    val allSteps = repository.getAllSteps().asLiveData()

    fun updateSteps(steps: Int) {
        viewModelScope.launch {
            repository.updateSteps(steps)
        }
    }
}
