package com.azrag.heyu.data.repository

import android.net.Uri
import com.azrag.heyu.data.model.Notice
import com.azrag.heyu.util.Result
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val noticeCollection = firestore.collection("notices")
    private val storage = FirebaseStorage.getInstance()

    suspend fun getAllNotices(): Result<List<Notice>> {
        return try {
            val snapshot = noticeCollection.orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            val notices = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notice::class.java)?.copy(id = doc.id)
            }
            Result.Success(notices)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to load notices.")
        }
    }

    suspend fun getNoticeById(id: String): Result<Notice> {
        return try {
            val document = noticeCollection.document(id).get().await()
            val notice = document.toObject(Notice::class.java)?.copy(id = document.id)
            if (notice != null) Result.Success(notice) else Result.Error("Notice not found.")
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to load notice.")
        }
    }

    suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            val fileName = "notices/${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val ref = storage.reference.child(fileName)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Image upload failed.")
        }
    }

    suspend fun addNotice(notice: Notice): Result<String> {
        val currentUser = Firebase.auth.currentUser ?: return Result.Error("User not logged in.")
        
        return try {
            val noticeMap = hashMapOf(
                "creatorId" to currentUser.uid,
                "creatorName" to notice.creatorName,
                "creatorImageUrl" to notice.creatorImageUrl,
                "title" to notice.title,
                "description" to notice.description,
                "category" to notice.category,
                "eventDate" to notice.eventDate,
                "eventTime" to notice.eventTime,
                "location" to notice.location,
                "imageUrl" to notice.imageUrl,
                "attendees" to emptyList<String>(),
                "timestamp" to FieldValue.serverTimestamp()
            )
            val documentRef = noticeCollection.add(noticeMap).await()
            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Firestore error.")
        }
    }

    suspend fun toggleNoticeParticipation(noticeId: String): Result<Unit> {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return Result.Error("Please log in.")
        return try {
            val docRef = noticeCollection.document(noticeId)
            val document = docRef.get().await()
            val attendees = document.get("attendees") as? List<String> ?: emptyList()

            if (attendees.contains(currentUserId)) {
                docRef.update("attendees", FieldValue.arrayRemove(currentUserId)).await()
            } else {
                docRef.update("attendees", FieldValue.arrayUnion(currentUserId)).await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Action failed: ${e.localizedMessage}")
        }
    }
}
