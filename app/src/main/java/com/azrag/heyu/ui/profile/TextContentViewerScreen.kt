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
        "terms" -> "Kullanım Koşulları" to "1. Uygulamayı etik kurallar çerçevesinde kullanın...\n2. Diğer kullanıcılara saygılı olun..."
        "privacy" -> "Gizlilik Politikası" to "Verileriniz Yeditepe Üniversitesi standartlarında korunmaktadır..."
        else -> "Bilgi" to "İçerik bulunamadı."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(text = content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
