package com.azrag.heyu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Event(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val organizer: String = "",
    val creatorName: String = "", // Eklendi
    val category: String = "Genel",
    val location: String = "",
    val creatorId: String = "",
    val imageUrl: String = "",
    val participants: List<String> = emptyList(), // attendees -> participants olarak mühürlendi

    val eventDate: String = "",
    val eventTime: String = "",

    @ServerTimestamp val serverTimestamp: Date? = null
)
