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

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")
    private val TAG = "DEBUG_HEYU"

    suspend fun loginUser(email: String, password: String): Result<Boolean> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.Error("Could not get user ID.")

            val profileDoc = usersCollection.document(uid).get().await()
            Result.Success(profileDoc.exists())
        } catch (e: Exception) {
            Log.e(TAG, "loginUser error: ${e.message}")
            Result.Error(e.message ?: "Login failed.")
        }
    }

    suspend fun saveUserProfile(profile: UserProfile, imageUri: Uri?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.Error("Not logged in.")
        return try {
            var finalProfile = profile.copy(id = uid)

            imageUri?.let { uri ->
                val storageRef = storage.reference.child("avatars/$uid.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                finalProfile = finalProfile.copy(photoUrl = downloadUrl)
            }

            usersCollection.document(uid).set(finalProfile, SetOptions.merge()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not update profile info.")
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection.document(profile.id).set(profile, SetOptions.merge()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Update failed.")
        }
    }

    suspend fun blockUser(targetUserId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.Error("No session.")
        return try {
            usersCollection.document(uid).update("blockedUsers", FieldValue.arrayUnion(targetUserId)).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Blocking failed.")
        }
    }

    suspend fun reportUser(targetUserId: String, reason: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.Error("No session.")
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
            Result.Error(e.message ?: "Could not send report.")
        }
    }

    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            Result.Success(snapshot.toObject(UserProfile::class.java))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not get profile info.")
        }
    }

    suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        val uid = auth.currentUser?.uid ?: return Result.Error("Not logged in.")
        return getUserProfile(uid)
    }

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
            Result.Error(e.message ?: "Could not send reset email.")
        }
    }

    // --- KEŞFET (SWIPE) FONKSİYONLARI ---

    suspend fun getDiscoverUsers(): Result<List<UserProfile>> {
        val currentUid = auth.currentUser?.uid ?: return Result.Error("Oturum bulunamadı.")
        return try {
            // 1. Mevcut kullanıcıyı çekip kimleri beğendiğini/geçtiğini öğreniyoruz
            val currentUserDoc = usersCollection.document(currentUid).get().await()
            val currentUser = currentUserDoc.toObject(UserProfile::class.java)

            val excludedIds = mutableListOf(currentUid) // Kendini gösterme
            currentUser?.let {
                excludedIds.addAll(it.likedUsers)
                excludedIds.addAll(it.passedUsers)
                excludedIds.addAll(it.blockedUsers)
            }

            // 2. Diğer kullanıcıları çekiyoruz
            val snapshot = usersCollection
                .whereEqualTo("onboardingComplete", true)
                .limit(40)
                .get().await()

            val users = snapshot.toObjects(UserProfile::class.java)
                .filter { it.id !in excludedIds }
                .shuffled() // Karışık gelsinler

            Result.Success(users)
        } catch (e: Exception) {
            Log.e(TAG, "getDiscoverUsers error: ${e.message}")
            Result.Error(e.message ?: "Kullanıcılar yüklenemedi.")
        }
    }

    suspend fun likeUser(targetUserId: String): Result<Boolean> {
        val currentUid = auth.currentUser?.uid ?: return Result.Error("Oturum bulunamadı.")
        return try {
            // 1. LikedUsers listesine ekle
            usersCollection.document(currentUid).update("likedUsers", FieldValue.arrayUnion(targetUserId)).await()

            // 2. Karşı taraf bizi daha önce beğenmiş mi bak (Match kontrolü)
            val targetUserDoc = usersCollection.document(targetUserId).get().await()
            val targetUser = targetUserDoc.toObject(UserProfile::class.java)

            if (targetUser?.likedUsers?.contains(currentUid) == true) {
                // EŞLEŞME OLDU!
                // Her iki tarafın matches listesine ekle
                usersCollection.document(currentUid).update("matches", FieldValue.arrayUnion(targetUserId)).await()
                usersCollection.document(targetUserId).update("matches", FieldValue.arrayUnion(currentUid)).await()
                
                // Chat odası oluşturma mantığı burada veya MatchAnimationScreen'de tetiklenebilir
                Result.Success(true) // True = Match oldu
            } else {
                Result.Success(false) // Beğenildi ama henüz match yok
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Beğeni işlemi başarısız.")
        }
    }

    suspend fun passUser(targetUserId: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.Error("Oturum bulunamadı.")
        return try {
            usersCollection.document(currentUid).update("passedUsers", FieldValue.arrayUnion(targetUserId)).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Geçme işlemi başarısız.")
        }
    }

    suspend fun seedDummyUsers(): Result<Unit> {
        val names = listOf("Alex", "Jordan", "Taylor", "Morgan", "Charlie", "Casey", "Riley", "Skyler", "Quinn", "Avery")
        val departments = listOf("Computer Engineering", "Psychology", "Business", "Architecture", "Medicine")
        val allHobbies = listOf("Gaming", "Cooking", "Photography", "Fitness", "Music", "Art", "Travel")

        return try {
            val batch = firestore.batch()
            for (i in 1..10) {
                val timestamp = System.currentTimeMillis()
                val id = "test_${timestamp}_$i"
                val dummyUser = UserProfile(
                    id = id,
                    displayName = names.random(),
                    email = "test${timestamp}_$i@std.yeditepe.edu.tr",
                    photoUrl = "https://ui-avatars.com/api/?name=${names.random()}&background=random",
                    department = departments.random(),
                    bio = "Hey! I'm a Yeditepe student. Let's match!",
                    hobbies = allHobbies.shuffled().take(3),
                    age = (18..25).random(),
                    onboardingComplete = true
                )
                val docRef = usersCollection.document(id)
                batch.set(docRef, dummyUser)
            }
            batch.commit().await()
            Log.d(TAG, "Successfully seeded 10 dummy users.")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Seed failed: ${e.message}", e)
            Result.Error(e.message ?: "Failed to seed users")
        }
    }
}
