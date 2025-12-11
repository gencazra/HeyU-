//----- Feedback.kt -----

package com.azrag.heyu.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Feedback(
    val userId: String = "",
    val message: String = "",
    @ServerTimestamp // Firebase, bu alana sunucu saatini otomatik ekleyecek
    val timestamp: Date? = null
)
