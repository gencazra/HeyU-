package com.azrag.heyu.util

/**
 * ModerationManager: Uygulama içi içerik güvenliğini sağlamak için
 * küfür, argo ve uygunsuz içerikleri filtreleyen yardımcı nesne.
 */
object ModerationManager {

    // Profesyonel ve akademik bir proje için filtrelenmiş uygunsuz terimler listesi
    private val forbiddenWords = listOf(
        // General Profanity (English)
        "fuck", "shit", "bitch", "bastard", "idiot", "dumb", "stupid",

        // Inappropriate Content & Adult Terms
        "porn", "sex", "nude", "erotic", "adult", "pornography", "violence",

        // Turkish Terms (Hafifletilmiş ve genel argo terimler)
        "amk", "lan", "aptal", "gerizekali", "kufur1", "argo2"
    )

    /**
     * Mesajın gönderilmeye uygun olup olmadığını kontrol eder.
     * Eğer listedeki ağır terimlerden birini içeriyorsa FALSE döner.
     */
    fun isSafe(text: String): Boolean {
        if (text.isBlank()) return true
        val lowerText = text.lowercase()
        // Herhangi bir yasaklı kelime geçiyor mu kontrol et
        return forbiddenWords.none { lowerText.contains(it) }
    }

    /**
     * Metni filtreler ve yasaklı kelimeleri yıldızlar (***).
     * Okul projesinde sunum yaparken metnin temiz görünmesini sağlar.
     */
    fun filterText(text: String): String {
        if (text.isBlank()) return text
        var filtered = text
        forbiddenWords.forEach { word ->
            // Regex(?i): Büyük-küçük harf duyarsız eşleşme sağlar
            val regex = Regex("(?i)$word")
            filtered = filtered.replace(regex, "***")
        }
        return filtered
    }
}
