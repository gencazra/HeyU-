
package com.azrag.heyu.data.model


import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val type: MessageType = MessageType.TEXT,
    val reactions: Map<String, List<String>> = emptyMap()
)

enum class MessageType {
    TEXT,
    IMAGE
}
