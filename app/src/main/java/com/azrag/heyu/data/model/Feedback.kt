// Dosya Yolu: data/model/Feedback.kt
package com.azrag.heyu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Kullanıcıların gönderdiği geri bildirimleri (öneri/şikayet) temsil eder.
 * Firestore'daki 'feedback' koleksiyonunda saklanır.
 */
// DOĞRU MODEL
data class Feedback(
    val id: String = "",
    val userId: String = "",
    val text: String = "", // <<< DOĞRU ALAN ADI
    @ServerTimestamp val timestamp: Date? = null
)
