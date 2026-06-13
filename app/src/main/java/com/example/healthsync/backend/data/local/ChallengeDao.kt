package com.example.healthsync.backend.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM joined_challenges ORDER BY joinedAt DESC")
    fun getAllJoinedChallenges(): Flow<List<JoinedChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: JoinedChallengeEntity)

    @Query("DELETE FROM joined_challenges WHERE challengeId = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM joined_challenges WHERE challengeId = :id)")
    fun isJoinedFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM joined_challenges WHERE challengeId = :id)")
    suspend fun isJoined(id: String): Boolean

    @Query("UPDATE joined_challenges SET progress = :progress, status = :status WHERE challengeId = :id")
    suspend fun updateProgress(id: String, progress: Int, status: String)
}
