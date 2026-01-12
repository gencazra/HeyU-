package com.azrag.heyu.data.repository

import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.util.MatchAlgorithm
import com.azrag.heyu.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")
    private val matchesCollection = firestore.collection("matches")
    private val chatsCollection = firestore.collection("chats")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""


    suspend fun getDiscoveryCandidates(currentUser: UserProfile): Result<List<UserProfile>> {
        if (currentUserId.isEmpty()) return Result.Error("Oturum süresi doldu.")

        return try {
            val myDoc = usersCollection.document(currentUserId).get().await()
            val liked = myDoc.get("likedUsers") as? List<String> ?: emptyList()
            val passed = myDoc.get("passedUsers") as? List<String> ?: emptyList()

            val seenIds = (liked + passed + listOf(currentUserId)).distinct()

            val querySnapshot = usersCollection.limit(100).get().await()

            val candidates = querySnapshot.toObjects(UserProfile::class.java)
                .filter { it.id !in seenIds && it.onboardingComplete }
                .map { targetUser ->
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
        val timestamp = FieldValue.serverTimestamp()

        val matchData = mapOf(
            "participants" to listOf(currentUserId, targetUid),
            "timestamp" to timestamp
        )
        matchesCollection.add(matchData).await()

        usersCollection.document(currentUserId)
            .update("matches", FieldValue.arrayUnion(targetUid)).await()

        usersCollection.document(targetUid)
            .update("matches", FieldValue.arrayUnion(currentUserId)).await()

        val chatId = if (currentUserId < targetUid) "${currentUserId}_${targetUid}" else "${targetUid}_${currentUserId}"

        val chatData = mapOf(
            "chatId" to chatId,
            "members" to listOf(currentUserId, targetUid),
            "lastMessage" to "Artık eşleştiniz! Merhaba deyin.",
            "lastMessageTimestamp" to timestamp
        )

        chatsCollection.document(chatId).set(chatData).await()
    }

    suspend fun passUser(passedUserId: String): Result<Unit> {
        if (currentUserId.isEmpty()) return Result.Error("Oturum süresi doldu.")
        return try {
            usersCollection.document(currentUserId)
                .update("passedUsers", FieldValue.arrayUnion(passedUserId)).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "İşlem kaydedilemedi.")
        }
    }
}
