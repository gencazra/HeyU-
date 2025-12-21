package com.azrag.heyu.util

import com.azrag.heyu.data.model.UserProfile

object MatchAlgorithm {
    /**
     * İki kullanıcı arasındaki ortak ilgi alanlarına göre uyum yüzdesini hesaplar.
     */
    fun calculateMatchScore(currentUser: UserProfile, targetUser: UserProfile): Int {
        if (currentUser.hobbies.isEmpty() || targetUser.hobbies.isEmpty()) return 0

        val commonHobbies = currentUser.hobbies.intersect(targetUser.hobbies.toSet())
        val totalUniqueHobbies = (currentUser.hobbies + targetUser.hobbies).distinct()

        // Jaccard Similarity benzeri basit bir yüzde hesabı
        val percentage = (commonHobbies.size.toDouble() / totalUniqueHobbies.size.toDouble()) * 100

        // En az %10 taban puan (aynı okulda oldukları için) + ilgi alanı bonusu
        return (percentage.toInt() + 10).coerceAtMost(100)
    }
}
