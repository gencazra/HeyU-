package com.azrag.heyu.data.repository

import com.azrag.heyu.data.model.Notice
import com.azrag.heyu.util.Result
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor(private val firestore: FirebaseFirestore) {
    private val noticeCollection = firestore.collection("notices")

    suspend fun getAllNotices(): Result<List<Notice>> {
        return try {
            val snapshot = noticeCollection.orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            val notices = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notice::class.java)?.copy(id = doc.id)
            }
            Result.Success(notices)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Duyurular yüklenemedi.")
        }
    }

    suspend fun getNoticeById(id: String): Result<Notice> {
        return try {
            val document = noticeCollection.document(id).get().await()
            val notice = document.toObject(Notice::class.java)?.copy(id = document.id)
            if (notice != null) Result.Success(notice) else Result.Error("Duyuru bulunamadı.")
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Duyuru yüklenemedi.")
        }
    }

    suspend fun addNotice(notice: Notice): Result<String> {
        val currentUser = Firebase.auth.currentUser ?: return Result.Error("Giriş yapmış kullanıcı bulunamadı.")
        
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
            Result.Error(e.localizedMessage ?: "Firestore hatası.")
        }
    }

    suspend fun toggleNoticeParticipation(noticeId: String): Result<Unit> {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return Result.Error("Giriş yapmalısınız.")
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
            Result.Error("İşlem başarısız: ${e.localizedMessage}")
        }
    }
}
