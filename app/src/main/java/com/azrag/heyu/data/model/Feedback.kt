
package com.azrag.heyu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class Feedback(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
