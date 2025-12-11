// Dosya: data/EventRepository.kt

package com.azrag.heyu.data

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val eventsCollection = firestore.collection("events")

    /**
     * Seçilen görseli Firebase Storage'a yükler ve indirme URL'sini döndürür.
     */
    suspend fun uploadEventImage(imageUri: Uri): String? {
        return try {
            val storageRef = storage.reference
            // Her resme benzersiz bir ID veriyoruz ki üzerine yazılmasın.
            val imageFileName = "event_images/${UUID.randomUUID()}"
            val imageRef = storageRef.child(imageFileName)
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await()?.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Yeni bir Event nesnesini Firestore'a kaydeder.
     */
    suspend fun addEvent(event: Event): String? {
        val currentUserId = auth.currentUser?.uid ?: return null
        return try {
            val newEventRef = eventsCollection.document()
            val finalEvent = event.copy(
                id = newEventRef.id,
                creatorId = currentUserId
            )
            newEventRef.set(finalEvent).await()
            newEventRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Tüm etkinlikleri tarihe göre en yeniden eskiye doğru sıralayarak getirir.
     */
    suspend fun getAllEvents(): List<Event> {
        return try {
            val snapshot = eventsCollection
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Event::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Verilen ID'ye sahip tek bir etkinliği Firestore'dan getirir.
     */
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val snapshot = eventsCollection.document(eventId).get().await()
            snapshot.toObject(Event::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Kullanıcının bir etkinliğe katılım durumunu değiştirir (katılır veya ayrılır).
     */
    suspend fun toggleParticipation(eventId: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false
        return try {
            val eventRef = eventsCollection.document(eventId)
            val event = getEventById(eventId) ?: return false
            val isParticipant = event.participantIds.contains(currentUserId)

            if (isParticipant) {
                // Eğer kullanıcı zaten katılımcıysa, listeden çıkar (Ayrılma işlemi)
                eventRef.update("participantIds", FieldValue.arrayRemove(currentUserId)).await()
            } else {
                // Eğer kullanıcı katılımcı değilse, listeye ekle (Katılma işlemi)
                eventRef.update("participantIds", FieldValue.arrayUnion(currentUserId)).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
