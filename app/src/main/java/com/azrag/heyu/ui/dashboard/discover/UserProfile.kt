//----- UserProfile.kt (CLASS_LEVEL EKLENMİŞ HALİ) -----

package com.azrag.heyu.ui.dashboard.discover

data class UserProfile(
    val fullName: String? = null,
    val photoUrl: String? = null,
    val faculty: String? = null,
    val major: String? = null,
    val classLevel: String? = null, // YENİ EKLENDİ: Sınıf seviyesi
    val bio: String? = null,
    val interests: List<String>? = null
)

