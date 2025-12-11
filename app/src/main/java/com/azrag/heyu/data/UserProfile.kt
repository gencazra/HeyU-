// Dosya: data/UserProfile.kt

package com.azrag.heyu.data

/**
 * Firestore'da saklanacak ve uygulama içinde kullanılacak kullanıcı modelimiz.
 */
data class UserProfile(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val department: String = "",
    val profileImageUrl: String = "", // Profil fotoğrafının URL'si
    val bio: String = "",
    val interests: List<String> = emptyList(), // İlgi alanları etiketleri
    val favoriteMusicGenres: List<String> = emptyList(),
    val favoriteMovieGenres: List<String> = emptyList()
)
