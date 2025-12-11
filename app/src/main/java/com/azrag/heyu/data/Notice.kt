// Dosya: data/Notice.kt

package com.azrag.heyu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore'daki 'notices' koleksiyonundaki bir dökümanı temsil eder.
 */
data class Notice(
    val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    @ServerTimestamp
    val date: Timestamp = Timestamp.now(),
    val participantIds: List<String> = emptyList() // "Ben de varım" diyenlerin listesi
)
