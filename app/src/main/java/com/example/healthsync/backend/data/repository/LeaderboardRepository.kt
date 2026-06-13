package com.example.healthsync.backend.data.repository

import com.example.healthsync.backend.firebase.FirestoreManager
import com.example.healthsync.frontend.models.UserRank
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LeaderboardRepository {
    fun getLeaderboard(): Flow<List<UserRank>> = callbackFlow {
        val listener = object : FirestoreManager.LeaderboardListener {
            override fun onLeaderboardLoaded(ranks: List<UserRank>) {
                trySend(ranks)
            }
        }
        FirestoreManager.loadLeaderboard(listener)
        awaitClose { }
    }
}
