//----- FeedbackScreen.kt (YENİ DOSYA) -----

package com.azrag.heyu.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeedbackViewModel = viewModel()
) {
    val message by viewModel.message.collectAsState()
    val sendState by viewModel.sendState.collectAsState()

    // Gönderme durumu değiştiğinde kontrol yap
    LaunchedEffect(sendState) {
        if (sendState is SendState.Success) {
            // Başarılı olursa 1.5 saniye bekle ve geri dön
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Öneri & Şikayet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri Dön")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Eğer gönderme başarılıysa, bir teşekkür mesajı göster
            if (sendState is SendState.Success) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Geri bildiriminiz için teşekkür ederiz!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                // Normal durumda metin kutusunu ve butonu göster
                Text(
                    "Uygulamamızla ilgili görüşlerinizi, önerilerinizi veya karşılaştığınız sorunları bize buradan iletebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = viewModel::onMessageChange,
                    label = { Text("Mesajınız...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Kalan tüm alanı kapla
                    enabled = sendState !is SendState.Sending
                )

                Button(
                    onClick = { viewModel.sendFeedback() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sendState !is SendState.Sending && message.isNotBlank()
                ) {
                    if (sendState is SendState.Sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Gönder")
                    }
                }

                // Hata mesajı varsa göster
                if (sendState is SendState.Error) {
                    Text(
                        text = (sendState as SendState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
