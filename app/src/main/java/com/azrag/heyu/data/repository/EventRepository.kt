package com.azrag.heyu.data.repository

import com.azrag.heyu.data.model.Event
import com.azrag.heyu.util.Result
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor() {
    private val eventsCollection = Firebase.firestore.collection("events")

    suspend fun getAllEvents(): Result<List<Event>> = try {
        val snapshot = eventsCollection
            .orderBy("serverTimestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { doc ->
            doc.toObject<Event>()?.copy(id = doc.id)
        }
        Result.Success(list)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Etkinlikler yüklenemedi.")
    }

    suspend fun addEvent(event: Event): Result<Unit> = try {
        val doc = eventsCollection.document()
        val eventMap = hashMapOf(
            "title" to event.title,
            "description" to event.description,
            "organizer" to event.organizer,
            "creatorName" to event.creatorName,
            "location" to event.location,
            "imageUrl" to event.imageUrl,
            "eventDate" to event.eventDate,
            "eventTime" to event.eventTime,
            "creatorId" to event.creatorId,
            "participants" to emptyList<String>(),
            "serverTimestamp" to FieldValue.serverTimestamp()
        )
        eventsCollection.document(doc.id).set(eventMap).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Etkinlik eklenemedi.")
    }

    suspend fun getEventById(eventId: String): Result<Event?> = try {
        val doc = eventsCollection.document(eventId).get().await()
        Result.Success(doc.toObject<Event>()?.copy(id = doc.id))
    } catch (e: Exception) {
        Result.Error(e.message ?: "Detay alınamadı.")
    }

    suspend fun toggleUserAttendance(eventId: String): Result<Unit> {
        val uid = Firebase.auth.currentUser?.uid ?: return Result.Error("Giriş yapın.")
        return try {
            val docRef = eventsCollection.document(eventId)
            val doc = docRef.get().await()
            val participants = (doc.get("participants") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            if (participants.contains(uid)) {
                docRef.update("participants", FieldValue.arrayRemove(uid)).await()
            } else {
                docRef.update("participants", FieldValue.arrayUnion(uid)).await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("İşlem başarısız.")
        }
    }
}
