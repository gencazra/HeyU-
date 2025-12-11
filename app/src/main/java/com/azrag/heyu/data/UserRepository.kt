// Dosya: data/UserRepository.kt

package com.azrag.heyu.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Bu sınıfın uygulama boyunca tek bir örneği olacağını belirtiyoruz (Hilt için)
@Singleton
class UserRepository @Inject constructor() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    // Şu anki giriş yapmış kullanıcının ID'sini döndürür
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Yeni bir kullanıcı profilini veya mevcut bir profili Firestore'a kaydeder.
     * @param userProfile Kaydedilecek kullanıcı verilerini içeren nesne.
     * @throws Exception Eğer kullanıcı giriş yapmamışsa veya işlem başarısız olursa.
     */
    suspend fun saveUserProfile(userProfile: UserProfile) {
        val userId = getCurrentUserId() ?: throw Exception("Kullanıcı giriş yapmamış.")

        // UserProfile nesnesini Firestore'a yazıyoruz.
        // Belge ID'si olarak kullanıcının Authentication ID'sini (uid) kullanıyoruz.
        // Bu sayede her kullanıcının tek bir profili olur.
        usersCollection.document(userId).set(userProfile).await()
    }

    /**
     * Belirtilen ID'ye sahip kullanıcı profilini Firestore'dan getirir.
     * @param userId Getirilecek kullanıcının ID'si.
     * @return UserProfile nesnesi veya null (eğer bulunamazsa).
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            // Hata durumunda veya kullanıcı yoksa null döner
            null
        }
    }
}
