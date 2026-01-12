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

@Singleton
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")
    private val matchesCollection = firestore.collection("matches")
    private val chatsCollection = firestore.collection("chats")

    private val currentUserId: String get() = auth.currentUser?.uid ?: ""

    suspend fun getDiscoveryCandidates(currentUser: UserProfile): Result<List<UserProfile>> {
        if (currentUserId.isEmpty()) return Result.Error("Session expired.")
        return try {
            val myDoc = usersCollection.document(currentUserId).get().await()
            val liked = myDoc.get("likedUsers") as? List<String> ?: emptyList()
            val passed = myDoc.get("passedUsers") as? List<String> ?: emptyList()
            val seenIds = (liked + passed + listOf(currentUserId)).distinct()

            val querySnapshot = usersCollection.whereEqualTo("onboardingComplete", true).limit(100).get().await()

            val candidates = querySnapshot.toObjects(UserProfile::class.java)
                .filter { it.id !in seenIds }
                .map { it.copy(matchScore = MatchAlgorithm.calculateMatchScore(currentUser, it)) }
                .sortedByDescending { it.matchScore }

            Result.Success(candidates)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error loading candidates")
        }
    }

    suspend fun likeUser(likedUserId: String): Result<Boolean> {
        try {
            usersCollection.document(currentUserId)
                .update("likedUsers", FieldValue.arrayUnion(likedUserId)).await()

            val likedUserDoc = usersCollection.document(likedUserId).get().await()
            val targetLikedList = likedUserDoc.get("likedUsers") as? List<String> ?: emptyList()

            return if (targetLikedList.contains(currentUserId)) {
                createChatRoom(likedUserId) // Eşleşme varsa odayı aç
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        } catch (e: Exception) {
            return Result.Error(e.message ?: "Like failed")
        }
    }

    private suspend fun createChatRoom(targetUid: String) {
        val chatId = if (currentUserId < targetUid) "${currentUserId}_${targetUid}" else "${targetUid}_${currentUserId}"
        val timestamp = FieldValue.serverTimestamp()

        val chatData = mapOf(
            "participants" to listOf(currentUserId, targetUid),
            "lastMessage" to "HeyU! You matched!",
            "lastMessageTimestamp" to timestamp
        )

        // 1. Chat odasını oluştur
        chatsCollection.document(chatId).set(chatData).await()

        // 2. "HeyU!" başlangıç mesajını messages sub-collection'ına ekle
        val initialMessage = mapOf(
            "senderId" to "system",
            "text" to "HeyU! It's a match! Start chatting now.",
            "timestamp" to timestamp
        )
        chatsCollection.document(chatId).collection("messages").add(initialMessage).await()
    }

    suspend fun passUser(passedUserId: String): Result<Unit> {
        return try {
            usersCollection.document(currentUserId).update("passedUsers", FieldValue.arrayUnion(passedUserId)).await()
            Result.Success(Unit)
        } catch (e: Exception) { Result.Error("Pass failed") }
    }
}
