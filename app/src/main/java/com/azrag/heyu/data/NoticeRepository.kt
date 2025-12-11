// Dosya: data/NoticeRepository.kt

package com.azrag.heyu.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val noticesCollection = firestore.collection("notices")

    /**
     * Yeni bir duyuruyu Firestore'a ekler.
     */
    suspend fun addNotice(notice: Notice): String? {
        val currentUserId = auth.currentUser?.uid ?: return null
        return try {
            val newNoticeRef = noticesCollection.document()
            val finalNotice = notice.copy(
                id = newNoticeRef.id,
                creatorId = currentUserId
            )
            newNoticeRef.set(finalNotice).await()
            newNoticeRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Tüm duyuruları tarihe göre en yeniden eskiye doğru getirir.
     */
    suspend fun getAllNotices(): List<Notice> {
        return try {
            noticesCollection
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await().toObjects(Notice::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Kullanıcının bir duyuruya katılımını ("Ben de varım") yönetir.
     */
    suspend fun toggleNoticeParticipation(noticeId: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false
        return try {
            val noticeRef = noticesCollection.document(noticeId)
            val notice = noticeRef.get().await().toObject(Notice::class.java) ?: return false
            val isParticipant = notice.participantIds.contains(currentUserId)

            if (isParticipant) {
                noticeRef.update("participantIds", FieldValue.arrayRemove(currentUserId)).await()
            } else {
                noticeRef.update("participantIds", FieldValue.arrayUnion(currentUserId)).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
