// Dosya Yolu: data/model/Message.kt
package com.azrag.heyu.data.model

// <<< DÜZELTME 1: 'java.util.Date' import'u SİLİNDİ, DOĞRU import EKLENDİ. >>>
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Sohbetlerdeki tek bir mesajı temsil eder.
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Timestamp? = null, // <<< KESİNLİKLE 'Timestamp?' OLMALI
    val type: MessageType = MessageType.TEXT,
    val reactions: Map<String, List<String>> = emptyMap()
)

/**
 * Mesajın türünü belirtir (Metin mi, Resim mi vb.).
 */
enum class MessageType {
    TEXT,
    IMAGE
}
