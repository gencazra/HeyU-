package com.azrag.heyu.data.repository

import com.azrag.heyu.data.model.Chat
import com.azrag.heyu.data.model.Message
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val chatsCollection = firestore.collection("chats")

    suspend fun createOrGetChatRoom(otherUserId: String): Result<String> {
        val currentUserId = auth.currentUser?.uid ?: return Result.Error("Oturum bulunamadı.")

        val chatRoomId = if (currentUserId < otherUserId) {
            "${currentUserId}_$otherUserId"
        } else {
            "${otherUserId}_$currentUserId"
        }

        return try {
            val docRef = chatsCollection.document(chatRoomId)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                val chatData = hashMapOf(
                    "participants" to listOf(currentUserId, otherUserId),
                    "createdAt" to System.currentTimeMillis(),
                    "lastMessage" to "Hey! 👋",
                    "lastMessageSenderId" to currentUserId,
                    "lastMessageTimestamp" to Timestamp.now(),
                    "unreadCount" to mapOf(currentUserId to 0, otherUserId to 0)
                )
                docRef.set(chatData).await()
            }
            Result.Success(chatRoomId)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Sohbet başlatılamadı.")
        }
    }

    suspend fun sendTextMessageToRoom(chatRoomId: String, receiverId: String, text: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val message = hashMapOf(
            "senderId" to currentUserId,
            "text" to text,
            "timestamp" to Timestamp.now()
        )

        try {
            val batch = firestore.batch()
            val chatDocRef = chatsCollection.document(chatRoomId)
            val messageDocRef = chatDocRef.collection("messages").document()

            batch.set(messageDocRef, message)

            batch.update(chatDocRef, mapOf(
                "lastMessage" to text,
                "lastMessageSenderId" to currentUserId,
                "lastMessageTimestamp" to Timestamp.now(),
                "unreadCount.$receiverId" to FieldValue.increment(1)
            ))

            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun sendWelcomeMessage() {
        val userId = auth.currentUser?.uid ?: return
        val systemId = "HeyU_Admin"
        val chatRoomId = "system_$userId"

        try {
            val chatDocRef = chatsCollection.document(chatRoomId)
            val snapshot = chatDocRef.get().await()

            if (!snapshot.exists()) {
                val welcomeText = "HeyU! ailesine hoş geldin! 👋 Yeni insanlarla tanışmaya ve keşfetmeye hemen başlayabilirsin. Keyifli vakit geçirmeni dileriz!"
                
                val chatData = hashMapOf(
                    "participants" to listOf(systemId, userId),
                    "createdAt" to System.currentTimeMillis(),
                    "lastMessage" to welcomeText,
                    "lastMessageSenderId" to systemId,
                    "lastMessageTimestamp" to Timestamp.now(),
                    "unreadCount" to mapOf(userId to 1, systemId to 0)
                )
                chatDocRef.set(chatData).await()

                val messageData = hashMapOf(
                    "senderId" to systemId,
                    "text" to welcomeText,
                    "timestamp" to Timestamp.now()
                )
                chatDocRef.collection("messages").add(messageData).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMessagesFromRoom(chatRoomId: String): Flow<List<Message>> = callbackFlow {
        val subscription = chatsCollection.document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markMessagesAsRead(chatRoomId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        try {
            chatsCollection.document(chatRoomId)
                .update("unreadCount.$currentUserId", 0)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllChatsForCurrentUser(): Flow<List<Chat>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: return@callbackFlow

        val subscription = chatsCollection
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    val chat = doc.toObject(Chat::class.java)?.apply {
                        chatRoomId = doc.id
                    }
                    chat
                } ?: emptyList()

                trySend(chats)
            }
        awaitClose { subscription.remove() }
    }
}
