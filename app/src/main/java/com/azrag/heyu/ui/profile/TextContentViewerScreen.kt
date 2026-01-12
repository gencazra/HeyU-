package com.azrag.heyu.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextContentViewerScreen(contentType: String, onNavigateBack: () -> Unit) {
    // Navigasyondan gelen contentType "privacy" ise aşağıdaki metin görünür
    val (title, content) = when (contentType) {
        "terms" -> "Terms of Use" to """
            1. User Conduct: Users must interact within ethical rules and Yeditepe University community guidelines.
            2. Respect: Harassment, bullying, or inappropriate behavior towards other users will result in an immediate ban.
            3. Accuracy: Users are responsible for providing accurate information on their profiles.
        """.trimIndent()

        "privacy" -> "Privacy Policy" to """
            Personal Data Protection Policy (heyU!)
            
            1. Data Controller: Developed for Yeditepe University students.
            2. Collected Data: Student email (@std.yeditepe.edu.tr), display name, department, age, and interests.
            3. Purpose: Your data is processed solely to provide accurate matching and campus socialization.
            4. Data Security: Data is securely stored on Google Firebase servers and is never shared with third parties.
            
            By using heyU!, you agree to these terms.
        """.trimIndent()

        else -> "Information" to "Content not found."
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = title, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(text = content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
