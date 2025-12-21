package com.azrag.heyu.data.repository

import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.util.MatchAlgorithm
import com.azrag.heyu.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")
    private val matchesCollection = firestore.collection("matches")

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    /**
     * Keşfet ekranı için adayları getirir ve her birine uyum skoru hesaplar.
     * Firestore 'whereNotIn' limitini (max 10 ID) aşmamak için take(10) mühürlemesi yapılmıştır.
     */
    suspend fun getDiscoveryCandidates(currentUser: UserProfile): Result<List<UserProfile>> {
        if (currentUserId.isEmpty()) return Result.Error("Oturum kapalı.")

        return try {
            // 1. Kendi dokümanımızdan etkileşime girdiğimiz ID'leri alalım
            val myDoc = usersCollection.document(currentUserId).get().await()
            val liked = myDoc.get("likedUsers") as? List<String> ?: emptyList()
            val passed = myDoc.get("passedUsers") as? List<String> ?: emptyList()

            // Kendimizi ve daha önce gördüğümüz kişileri filtre listesine alalım
            val seenIds = (liked + passed + currentUserId).distinct()

            // 2. Adayları Firestore'dan çekelim
            val query = if (seenIds.isEmpty()) {
                usersCollection.limit(20)
            } else {
                // Firestore NOT_IN limitasyonu nedeniyle en güncel 10 ID'yi filtreliyoruz
                usersCollection.whereNotIn(FieldPath.documentId(), seenIds.take(10)).limit(20)
            }

            val querySnapshot = query.get().await()

            // 3. Algoritmayı kullanarak her adaya uyum skoru ata ve sırala
            val candidates = querySnapshot.toObjects(UserProfile::class.java).map { targetUser ->
                val score = MatchAlgorithm.calculateMatchScore(currentUser, targetUser)
                targetUser.copy(matchScore = score)
            }.sortedByDescending { it.matchScore }

            Result.Success(candidates)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Keşfet listesi yüklenirken bir hata oluştu.")
        }
    }

    /**
     * Bir kullanıcıyı beğenir. Eğer karşı taraf da beğenmişse eşleşme (match) başlatır.
     */
    suspend fun likeUser(likedUserId: String): Result<Boolean> {
        if (currentUserId.isEmpty()) return Result.Error("Yetkisiz işlem.")

        return try {
            // Kendi likedUsers listemize ekleyelim
            usersCollection.document(currentUserId)
                .update("likedUsers", FieldValue.arrayUnion(likedUserId)).await()

            // Karşı tarafın bizi beğenip beğenmediğini kontrol edelim
            val likedUserDoc = usersCollection.document(likedUserId).get().await()
            val likedUserLikedList = likedUserDoc.get("likedUsers") as? List<String> ?: emptyList()

            return if (likedUserLikedList.contains(currentUserId)) {
                // Karşılıklı beğeni var! Eşleşme işlemlerini başlat
                executeMatchSequence(likedUserId)
                Result.Success(true) // true = Eşleşme gerçekleşti diyalog göster
            } else {
                Result.Success(false) // false = Beğenildi ama henüz karşı taraftan beğeni yok
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Beğenme işlemi başarısız.")
        }
    }

    /**
     * Eşleşme gerçekleştiğinde Firestore'da gerekli güncellemeleri yapar.
     */
    private suspend fun executeMatchSequence(targetUid: String) {
        val matchData = mapOf(
            "participants" to listOf(currentUserId, targetUid),
            "timestamp" to FieldValue.serverTimestamp()
        )

        // 1. Matches koleksiyonuna yeni eşleşme dokümanı ekle
        matchesCollection.add(matchData).await()

        // 2. Her iki kullanıcının profilindeki 'matches' listesini güncelle
        usersCollection.document(currentUserId)
            .update("matches", FieldValue.arrayUnion(targetUid)).await()

        usersCollection.document(targetUid)
            .update("matches", FieldValue.arrayUnion(currentUserId)).await()
    }

    /**
     * Kullanıcıyı pas geçer (Sola kaydırma).
     */
    suspend fun passUser(passedUserId: String): Result<Unit> {
        if (currentUserId.isEmpty()) return Result.Error("Oturum kapalı.")
        return try {
            usersCollection.document(currentUserId)
                .update("passedUsers", FieldValue.arrayUnion(passedUserId)).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "İşlem kaydedilemedi.")
        }
    }
}
