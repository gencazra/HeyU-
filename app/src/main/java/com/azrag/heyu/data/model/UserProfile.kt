package com.azrag.heyu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp


data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val department: String = "",
    val bio: String = "",
    val hobbies: List<String> = emptyList(),

    val age: Int = 0,
    val birthYear: Int? = null,


    val likedUsers: List<String> = emptyList(),
    val passedUsers: List<String> = emptyList(),
    val matches: List<String> = emptyList(),

    val matchScore: Int = 0,

    val blockedUsers: List<String> = emptyList(),
    val reportedBy: List<String> = emptyList(),

    val isAdmin: Boolean = false,

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @get:Exclude
    val isOnline: Boolean = false
) {

    @Exclude
    fun isValidAge(): Boolean = age >= 18


    @Exclude
    fun isOnboardingComplete(): Boolean {
        return displayName.isNotBlank() &&
                department.isNotBlank() &&
                photoUrl.isNotBlank() &&
                isValidAge()
    }


    @Exclude
    fun isYeditepeEmail(): Boolean {
        return email.endsWith("@std.yeditepe.edu.tr") || email.endsWith("@yeditepe.edu.tr")
    }

    @Exclude
    fun calculateMatchScoreWith(other: UserProfile): Int {
        if (this.hobbies.isEmpty() || other.hobbies.isEmpty()) return 0
        val commonHobbies = this.hobbies.intersect(other.hobbies.toSet()).size
        return (commonHobbies * 100) / this.hobbies.size
    }

    @Exclude
    fun isBlocked(userId: String): Boolean {
        return blockedUsers.contains(userId)
    }
}
