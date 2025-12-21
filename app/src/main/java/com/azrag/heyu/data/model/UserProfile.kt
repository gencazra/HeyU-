package com.azrag.heyu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

/**
 * HeyU Kullanıcı Profili Modeli.
 * Yeditepe topluluk özellikleri (bölüm, hobi, engelleme) için mühürlenmiştir.
 */
data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val department: String = "", // Onboarding'deki 'major' buraya karşılık gelir
    val bio: String = "",
    val hobbies: List<String> = emptyList(),

    // OnboardingViewModel ile tam uyum için
    val age: Int = 0,
    val birthYear: Int? = null,

    // Sosyal Etkileşim Listeleri
    val likedUsers: List<String> = emptyList(),
    val passedUsers: List<String> = emptyList(),
    val matches: List<String> = emptyList(),

    // Algoritma Skoru (Discover ekranında hesaplanır)
    val matchScore: Int = 0,

    // Güvenlik ve Moderasyon (Mühürlendi)
    val blockedUsers: List<String> = emptyList(),
    val reportedBy: List<String> = emptyList(), // Raporlayanları takip etmek için

    val isAdmin: Boolean = false,

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @get:Exclude
    val isOnline: Boolean = false
) {
    /**
     * Firebase'den veri okurken yaşın geçerli olup olmadığını kontrol eder.
     */
    @Exclude
    fun isValidAge(): Boolean = age >= 18

    /**
     * Kullanıcının profilinin onboarding sürecinden geçip geçmediğini mühürler.
     */
    @Exclude
    fun isOnboardingComplete(): Boolean {
        return displayName.isNotBlank() &&
                department.isNotBlank() &&
                photoUrl.isNotBlank() &&
                isValidAge()
    }

    /**
     * Yeditepe Mail Kontrolü
     */
    @Exclude
    fun isYeditepeEmail(): Boolean {
        return email.endsWith("@std.yeditepe.edu.tr") || email.endsWith("@yeditepe.edu.tr")
    }

    /**
     * İki kullanıcı arasındaki hobi bazlı eşleşme yüzdesini hesaplar.
     */
    @Exclude
    fun calculateMatchScoreWith(other: UserProfile): Int {
        if (this.hobbies.isEmpty() || other.hobbies.isEmpty()) return 0
        val commonHobbies = this.hobbies.intersect(other.hobbies.toSet()).size
        return (commonHobbies * 100) / this.hobbies.size
    }

    /**
     * Kullanıcının engellenip engellenmediğini kontrol eder.
     */
    @Exclude
    fun isBlocked(userId: String): Boolean {
        return blockedUsers.contains(userId)
    }
}
