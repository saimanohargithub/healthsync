package com.example.healthsync.backend.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "joined_challenges")
data class JoinedChallengeEntity(
    @PrimaryKey val challengeId: String,
    val challengeName: String,
    val progress: Int,
    val target: Int,
    val status: String, // ACTIVE, COMPLETED
    val joinedAt: Long,
    val rewardPoints: Int,
    val type: String
)
