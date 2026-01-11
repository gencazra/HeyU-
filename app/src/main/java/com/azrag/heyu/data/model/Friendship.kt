package com.azrag.heyu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Friendship(
    val id: String = "",
    val p1Id: String = "", 
    val p2Id: String = "", 
    @ServerTimestamp val createdAt: Date? = null
)
