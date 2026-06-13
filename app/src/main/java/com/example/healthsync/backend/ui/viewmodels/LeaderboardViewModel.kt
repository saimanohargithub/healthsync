package com.example.healthsync.backend.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthsync.backend.data.repository.LeaderboardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class LeaderboardViewModel(private val repository: LeaderboardRepository) : ViewModel() {

    val leaderboardRanks = repository.getLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        .asLiveData()
}
