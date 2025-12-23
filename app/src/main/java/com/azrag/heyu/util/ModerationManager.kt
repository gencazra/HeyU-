package com.azrag.heyu.util

object ModerationManager {

    private val forbiddenWords = listOf(
        "fuck", "shit", "bitch", "bastard", "idiot", "dumb", "stupid",

        "porn", "sex", "nude", "erotic", "adult", "pornography", "violence",

        "amk", "lan", "aptal", "gerizekali", "kufur1", "argo2"
    )


    fun isSafe(text: String): Boolean {
        if (text.isBlank()) return true
        val lowerText = text.lowercase()
        return forbiddenWords.none { lowerText.contains(it) }
    }


    fun filterText(text: String): String {
        if (text.isBlank()) return text
        var filtered = text
        forbiddenWords.forEach { word ->
            val regex = Regex("(?i)$word")
            filtered = filtered.replace(regex, "***")
        }
        return filtered
    }
}
