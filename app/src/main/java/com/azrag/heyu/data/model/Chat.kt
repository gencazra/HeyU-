package com.azrag.heyu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Chat(
    @get:Exclude var chatRoomId: String = "",
    @get:Exclude var otherUser: UserProfile = UserProfile(),
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageSenderId: String? = null,
    val lastMessageTimestamp: Timestamp? = null,

    val unreadCount: Map<String, Int> = emptyMap()
)
