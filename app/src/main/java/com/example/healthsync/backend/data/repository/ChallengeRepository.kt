package com.example.healthsync.backend.data.repository

import android.util.Log
import com.example.healthsync.backend.data.local.ChallengeDao
import com.example.healthsync.backend.data.local.JoinedChallengeEntity
import com.example.healthsync.backend.firebase.FirestoreManager
import com.example.healthsync.frontend.models.Challenge
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChallengeRepository(private val challengeDao: ChallengeDao) {
    private val auth = FirebaseAuth.getInstance()
    private val userId get() = auth.currentUser?.uid

    fun getJoinedChallenges(): Flow<List<Challenge>> {
        return challengeDao.getAllJoinedChallenges().map { entities ->
            entities.map { entity ->
                val c = Challenge(
                    entity.challengeId,
                    entity.challengeName,
                    entity.target,
                    entity.rewardPoints,
                    0,
                    entity.type
                )
                c.currentProgress = entity.progress
                c.setJoined(true)
                c.setCompleted(entity.status == "COMPLETED")
                c.joinedAt = entity.joinedAt
                c
            }
        }
    }

    suspend fun joinChallenge(challenge: Challenge): Boolean {
        val uid = userId ?: return false
        Log.d("CHALLENGE_JOIN", "Joining challenge: ${challenge.id}")
        
        return withContext(Dispatchers.IO) {
            try {
                if (challengeDao.isJoined(challenge.id)) {
                    Log.d("CHALLENGE_JOIN", "Already joined locally")
                    return@withContext true
                }

                // 1. Save to Firestore
                FirestoreManager.joinChallenge(uid, challenge) {
                    Log.d("CHALLENGE_FIRESTORE_SAVE", "Firestore save callback triggered")
                }

                // 2. Save to Room
                val entity = JoinedChallengeEntity(
                    challengeId = challenge.id,
                    challengeName = challenge.name,
                    progress = 0,
                    target = challenge.goal,
                    status = "ACTIVE",
                    joinedAt = System.currentTimeMillis(),
                    rewardPoints = challenge.rewardPoints,
                    type = challenge.type
                )
                challengeDao.insert(entity)
                Log.d("CHALLENGE_ROOM_SAVE", "Saved to Room: ${challenge.id}")
                true
            } catch (e: Exception) {
                Log.e("CHALLENGE_JOIN", "Error joining challenge", e)
                false
            }
        }
    }

    fun isChallengeJoined(challengeId: String): Flow<Boolean> {
        return challengeDao.isJoinedFlow(challengeId)
    }

    fun startRealtimeSync(scope: CoroutineScope) {
        val uid = userId ?: return
        FirestoreManager.listenToJoinedChallenges(uid) { challenges ->
            scope.launch(Dispatchers.IO) {
                challenges.forEach { c ->
                    val entity = JoinedChallengeEntity(
                        challengeId = c.id,
                        challengeName = c.name,
                        progress = c.currentProgress,
                        target = c.goal,
                        status = if (c.isCompleted) "COMPLETED" else "ACTIVE",
                        joinedAt = System.currentTimeMillis(),
                        rewardPoints = c.rewardPoints,
                        type = c.type
                    )
                    challengeDao.insert(entity)
                }
            }
        }
    }
}
