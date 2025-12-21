package com.azrag.heyu.data.repository

import android.net.Uri
import android.util.Log
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * heyU - Kullanıcı Veri Yönetimi
 * Giriş, Profil, Moderasyon ve Güvenlik işlemleri mühürlenmiştir.
 */
@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")
    private val TAG = "DEBUG_HEYU"

    /**
     * Firebase Auth ile giriş yapar ve Firestore profil dökümanını kontrol eder.
     */
    suspend fun loginUser(email: String, password: String): Result<Boolean> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.Error("Kullanıcı kimliği alınamadı.")

            val profileDoc = usersCollection.document(uid).get().await()
            // Profil dökümanı varsa true döner, Onboarding'e gerek kalmaz.
            Result.Success(profileDoc.exists())
        } catch (e: Exception) {
            Log.e(TAG, "loginUser error: ${e.message}")
            Result.Error(e.message ?: "Giriş işlemi başarısız oldu.")
        }
    }

    /**
     * Kullanıcı profilini ve avatar görselini kaydeder.
     */
    suspend fun saveUserProfile(profile: UserProfile, imageUri: Uri?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.Error("Oturum açılmamış.")
        return try {
            var finalProfile = profile.copy(id = uid)

            // Görsel seçilmişse Storage'a yükle ve URL'i profile ekle
            imageUri?.let { uri ->
                val storageRef = storage.reference.child("avatars/$uid.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                finalProfile = finalProfile.copy(photoUrl = downloadUrl)
            }

            usersCollection.document(uid).set(finalProfile, SetOptions.merge()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Profil bilgileri güncellenemedi.")
        }
    }

    /**
     * Kullanıcıyı engellenenler listesine mühürler.
     */
    suspend fun blockUser(targetUserId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.Error("Oturum yok.")
        return try {
            usersCollection.document(uid).update("blockedUsers", FieldValue.arrayUnion(targetUserId)).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Engelleme işlemi başarısız.")
        }
    }

    /**
     * Uygunsuz içeriği raporlama sistemine ekler.
     */
    suspend fun reportUser(targetUserId: String, reason: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.Error("Oturum yok.")
        return try {
            val report = mapOf(
                "reporterId" to uid,
                "reportedId" to targetUserId,
                "reason" to reason,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("reports").add(report).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Rapor gönderilemedi.")
        }
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            Result.Success(snapshot.toObject(UserProfile::class.java))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Profil bilgisi alınamadı.")
        }
    }

    suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        val uid = auth.currentUser?.uid ?: return Result.Error("Oturum açılmamış.")
        return getUserProfile(uid)
    }

    /**
     * Profil değişikliklerini anlık olarak dinleyen akış (Flow).
     */
    fun getUserProfileStream(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Stream error: ${error.message}")
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserProfile::class.java))
        }
        awaitClose { listener.remove() }
    }

    fun getCurrentUserProfileStream(): Flow<UserProfile?> {
        val uid = auth.currentUser?.uid ?: ""
        return getUserProfileStream(uid)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Sıfırlama e-postası gönderilemedi.")
        }
    }
}
