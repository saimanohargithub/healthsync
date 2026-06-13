package com.example.healthsync.backend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthsync.backend.data.repository.HealthRepository
import com.example.healthsync.backend.firebase.FirestoreManager
import com.example.healthsync.frontend.models.Challenge
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: HealthRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    val userProfile = repository.getUserProfile().asLiveData()

    val userEmail = flow {
        emit(auth.currentUser?.email ?: "No Email")
    }.asLiveData()

    val totalWaterLogged = repository.getTotalWater()
        .map { (it ?: 0) / 1000f }
        .asLiveData()

    val completedChallenges = repository.getCompletedChallengesCount().asLiveData()

    val daysActive = flow {
        val creationTime = auth.currentUser?.metadata?.creationTimestamp ?: System.currentTimeMillis()
        val diff = System.currentTimeMillis() - creationTime
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        emit(days)
    }.asLiveData()

    val bmi = repository.getUserProfile().map { user ->
        if (user == null || user.heightCm <= 0) 0f
        else {
            val heightM = user.heightCm / 100f
            user.weightKg / (heightM * heightM)
        }
    }.asLiveData()

    init {
        syncProfile()
    }

    private fun syncProfile() {
        viewModelScope.launch {
            repository.syncProfileWithCloud()
        }
    }
}
