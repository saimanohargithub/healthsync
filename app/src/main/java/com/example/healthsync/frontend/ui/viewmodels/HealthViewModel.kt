package com.example.healthsync.frontend.ui.viewmodels;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.healthsync.frontend.data.local.UserEntity
import com.example.healthsync.frontend.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    val userProfile: StateFlow<UserEntity?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayWater: StateFlow<Int?> = repository.getTodayWater()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todaySleep = repository.getTodaySleep()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayStress = repository.getTodayStress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestSleepEntry = repository.getLatestSleepEntry().asLiveData()
    val weeklySleepEntries = repository.getWeeklySleepEntries().asLiveData()

    init {
        viewModelScope.launch {
            repository.syncProfileWithCloud()
        }
    }

    fun logSleepEntry(hours: Int, minutes: Int, score: Int) {
        viewModelScope.launch {
            repository.logSleepEntry(hours, minutes, score)
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.logWater(amountMl)
        }
    }

    fun logSleep(hours: Float, score: Int) {
        viewModelScope.launch {
            repository.logSleep(hours, score)
        }
    }

    fun logMood(mood: String, note: String) {
        viewModelScope.launch {
            repository.logMood(mood, note)
        }
    }
}
