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
    val (title, content) = when (contentType) {
        "terms" -> "Terms of Use" to """
            1. User Conduct: Users must interact within ethical rules and Yeditepe University community guidelines.
            2. Respect: Harassment, bullying, or inappropriate behavior towards other users will result in an immediate ban.
            3. Accuracy: Users are responsible for providing accurate information (department, age, etc.) on their profiles.
            4. Service: heyU! is a platform for campus socialization; any commercial or illegal activity is strictly prohibited.
        """.trimIndent()

        "privacy" -> "Clarification Text & Privacy Policy" to """
            Personal Data Protection Policy (heyU!)
            
            1. Data Controller: This application is developed for Yeditepe University students. Your data is managed by the heyU! team.
            
            2. Collected Data: We collect your Yeditepe University student email (@std.yeditepe.edu.tr), display name, department, age, and social interests.
            
            3. Purpose of Processing: Your data is processed solely to provide accurate matching with other students and to enhance campus socialization.
            
            4. Data Security: Your personal information is securely stored on Google Firebase servers and is never shared with third-party organizations.
            
            5. Your Rights: Under data protection regulations, you have the right to request the correction or deletion of your data at any time through the "Delete Account" or "Contact Us" options.
            
            By using heyU!, you agree to these terms and the processing of your data as described above.
        """.trimIndent()

        else -> "Information" to "Content not found."
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
//