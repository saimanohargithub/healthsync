package com.example.healthsync.backend.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthsync.backend.data.repository.ChallengeRepository
import com.example.healthsync.backend.data.repository.CommunityRepository
import com.example.healthsync.backend.data.repository.HealthRepository
import com.example.healthsync.backend.data.repository.LeaderboardRepository
import com.example.healthsync.backend.firebase.FirestoreManager
import com.example.healthsync.frontend.models.Challenge
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val healthRepository: HealthRepository,
    private val challengeRepository: ChallengeRepository,
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _joinStatus = MutableLiveData<String>()
    val joinStatus = _joinStatus

    init {
        challengeRepository.startRealtimeSync(viewModelScope)
    }

    val activeChallenges = challengeRepository.getJoinedChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        .asLiveData()

    val communityFeed = communityRepository.getCommunityFeed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        .asLiveData()

    val leaderboardRanks = leaderboardRepository.getLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        .asLiveData()

    val userBadges = callbackFlow<List<String>> {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            val listener = object : FirestoreManager.BadgeListener {
                override fun onBadgesLoaded(badges: List<String>) {
                    trySend(badges)
                }
            }
            FirestoreManager.loadUserBadges(uid, listener)
        } else {
            trySend(emptyList())
        }
        awaitClose { }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    .asLiveData()

    val communityStats = callbackFlow<Map<String, String>> {
        FirestoreManager.getGlobalStats { stats ->
            trySend(stats)
        }
        awaitClose { }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    .asLiveData()

    // Status for discoverable challenges
    val isStepsJoined = challengeRepository.isChallengeJoined("steps_10k")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        .asLiveData()

    val isHydrationJoined = challengeRepository.isChallengeJoined("hyd_hero")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        .asLiveData()

    fun joinChallenge(id: String, name: String, goal: Int, reward: Int, type: String) {
        viewModelScope.launch {
            _joinStatus.postValue("JOINING")
            val challenge = Challenge(id, name, goal, reward, 0, type)
            val success = challengeRepository.joinChallenge(challenge)
            if (success) {
                Log.d("CHALLENGE_UI_REFRESH", "Challenge $id joined successfully")
                _joinStatus.postValue("SUCCESS")
            } else {
                _joinStatus.postValue("ERROR")
            }
        }
    }

    fun joinHydrationHero() = joinChallenge("hyd_hero", "Hydration Hero", 3000, 50, "water")
    fun join10kSteps() = joinChallenge("steps_10k", "10K Steps Daily", 10000, 60, "steps")
}
