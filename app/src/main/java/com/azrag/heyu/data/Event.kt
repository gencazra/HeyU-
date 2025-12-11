// Dosya: data/Event.kt (YENİ OLUŞTURULAN DOSYA)

package com.azrag.heyu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Firestore'daki 'events' koleksiyonundaki bir dökümanı temsil eden veri modelidir.
 *
 * @param id Etkinliğin benzersiz ID'si (Firestore tarafından otomatik oluşturulur).
 * @param creatorId Etkinliği oluşturan kullanıcının ID'si.
 * @param title Etkinliğin başlığı.
 * @param description Etkinliğin detaylı açıklaması.
 * @param location Etkinliğin yapılacağı yer.
 * @param category Etkinliğin kategorisi (örn: "Spor", "Müzik").
 * @param date Etkinliğin tarihi ve saati. @ServerTimestamp, Firestore'a kaydedilirken sunucu zamanını otomatik alır.
 * @param imageUrl Etkinlik için yüklenen görselin Firebase Storage'daki URL'si.
 * @param participantIds Etkinliğe katılan kullanıcıların ID listesi.
 */
data class Event(
    val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val category: String = "",
    @ServerTimestamp
    val date: Timestamp = Timestamp.now(),
    val imageUrl: String = "",
    val participantIds: List<String> = emptyList()
)
