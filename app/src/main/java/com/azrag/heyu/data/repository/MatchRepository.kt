package com.azrag.heyu.data.repository

import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.util.MatchAlgorithm
import com.azrag.heyu.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")
    private val matchesCollection = firestore.collection("matches")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""


    suspend fun getDiscoveryCandidates(currentUser: UserProfile): Result<List<UserProfile>> {
        if (currentUserId.isEmpty()) return Result.Error("Oturum kapalı.")

        return try {
            val myDoc = usersCollection.document(currentUserId).get().await()
            val liked = myDoc.get("likedUsers") as? List<String> ?: emptyList()
            val passed = myDoc.get("passedUsers") as? List<String> ?: emptyList()

            val seenIds = (liked + passed + currentUserId).distinct()

            val query = if (seenIds.isEmpty()) {
                usersCollection.limit(20)
            } else {
                usersCollection.whereNotIn(FieldPath.documentId(), seenIds.take(10)).limit(20)
            }

            val querySnapshot = query.get().await()

            val candidates = querySnapshot.toObjects(UserProfile::class.java).map { targetUser ->
                val score = MatchAlgorithm.calculateMatchScore(currentUser, targetUser)
                targetUser.copy(matchScore = score)
            }.sortedByDescending { it.matchScore }

            Result.Success(candidates)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Keşfet listesi yüklenirken bir hata oluştu.")
        }
    }

    suspend fun likeUser(likedUserId: String): Result<Boolean> {
        if (currentUserId.isEmpty()) return Result.Error("Yetkisiz işlem.")

        return try {

            usersCollection.document(currentUserId)
                .update("likedUsers", FieldValue.arrayUnion(likedUserId)).await()

            val likedUserDoc = usersCollection.document(likedUserId).get().await()
            val likedUserLikedList = likedUserDoc.get("likedUsers") as? List<String> ?: emptyList()

            return if (likedUserLikedList.contains(currentUserId)) {
                executeMatchSequence(likedUserId)
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Beğenme işlemi başarısız.")
        }
    }

    private suspend fun executeMatchSequence(targetUid: String) {
        val matchData = mapOf(
            "participants" to listOf(currentUserId, targetUid),
            "timestamp" to FieldValue.serverTimestamp()
        )


        matchesCollection.add(matchData).await()


        usersCollection.document(currentUserId)
            .update("matches", FieldValue.arrayUnion(targetUid)).await()

        usersCollection.document(targetUid)
            .update("matches", FieldValue.arrayUnion(currentUserId)).await()
    }


    suspend fun passUser(passedUserId: String): Result<Unit> {
        if (currentUserId.isEmpty()) return Result.Error("Oturum kapalı.")
        return try {
            usersCollection.document(currentUserId)
                .update("passedUsers", FieldValue.arrayUnion(passedUserId)).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "İşlem kaydedilemedi.")
        }
    }
}
