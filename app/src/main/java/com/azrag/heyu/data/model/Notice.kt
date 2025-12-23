
package com.azrag.heyu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notice(
    var id: String = "",
    val creatorId: String = "",

    val title: String = "",
    val description: String = "",
    val category: String = "",

    val creatorName: String = "",
    val creatorImageUrl: String = "",

    val attendees: List<String> = emptyList(),

    @ServerTimestamp val timestamp: Date? = null
)
