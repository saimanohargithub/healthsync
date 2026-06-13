package com.example.healthsync.backend.data.repository

import com.example.healthsync.backend.firebase.FirestoreManager
import com.example.healthsync.frontend.models.Challenge
import com.example.healthsync.frontend.models.CommunityActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CommunityRepository {
    private val auth = FirebaseAuth.getInstance()
    private val userId get() = auth.currentUser?.uid

    fun getActiveChallenges(): Flow<List<Challenge>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        FirestoreManager.listenToJoinedChallenges(uid) { challenges ->
            trySend(challenges)
        }
        awaitClose { }
    }

    fun getCommunityFeed(): Flow<List<CommunityActivity>> = callbackFlow {
        FirestoreManager.loadCommunityFeed { activities ->
            trySend(activities)
        }
        awaitClose { }
    }

    fun joinChallenge(challenge: Challenge) {
        userId?.let { uid ->
            FirestoreManager.joinChallenge(uid, challenge) {
                // Success listener
            }
        }
    }
}
