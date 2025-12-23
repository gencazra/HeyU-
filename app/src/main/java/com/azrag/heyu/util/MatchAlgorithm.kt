package com.azrag.heyu.util

import com.azrag.heyu.data.model.UserProfile

object MatchAlgorithm {

    fun calculateMatchScore(currentUser: UserProfile, targetUser: UserProfile): Int {
        if (currentUser.hobbies.isEmpty() || targetUser.hobbies.isEmpty()) return 0

        val commonHobbies = currentUser.hobbies.intersect(targetUser.hobbies.toSet())
        val totalUniqueHobbies = (currentUser.hobbies + targetUser.hobbies).distinct()

        val percentage = (commonHobbies.size.toDouble() / totalUniqueHobbies.size.toDouble()) * 100

        return (percentage.toInt() + 10).coerceAtMost(100)
    }
}
