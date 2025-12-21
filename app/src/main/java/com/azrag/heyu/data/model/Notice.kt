// Dosya Yolu: data/model/Notice.kt
package com.azrag.heyu.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Bir duyuruyu temsil eder.
 * Firestore'daki 'notices' koleksiyonunda saklanır.
 */
data class Notice(
    var id: String = "",
    val creatorId: String = "",

    // UI'da gösterilecek temel bilgiler
    val title: String = "",
    val description: String = "",
    val category: String = "",

    // Kullanıcı profili bilgileri (denormalize edilmiş veri)
    val creatorName: String = "",
    val creatorImageUrl: String = "",

    // "Ben de varım!" diyenlerin UserID listesi
    val attendees: List<String> = emptyList(),

    @ServerTimestamp val timestamp: Date? = null
)
