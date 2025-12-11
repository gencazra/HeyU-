// Dosya: EditProfileScreen.kt

package com.azrag.heyu.ui.profile // Paket adının doğru olduğundan emin ol

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // GEREKLİ IMPORT
import com.azrag.heyu.ui.theme.HeyuTheme // GEREKLİ IMPORT
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // ViewModel'den DÜZENLEME state'ini dinle
    val editState by viewModel.editState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profili Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveChanges(
                                onSuccess = { onNavigateBack() }, // Kaydedince geri dön
                                onError = { /* Hata mesajı gösterilebilir */ }
                            )
                        },
                        enabled = !editState.isSaving // Kaydederken butonu pasif yap
                    ) {
                        if (editState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Kaydet")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Sayfa dikeyde kaysın
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bio Düzenleme Alanı
            OutlinedTextField(
                value = editState.bio,
                onValueChange = { newBio -> viewModel.onBioChange(newBio) },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // İlgi Alanları Düzenleme Alanı
            Text("İlgi Alanların", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Örnek ilgi alanları, bunlar normalde başka bir yerden gelir
            val allInterests = listOf("#yapayzeka", "#sinema", "#tasarım", "#basketbol", "#müzik", "#oyun")

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                allInterests.forEach { interest ->
                    val isSelected = editState.selectedInterests.contains(interest)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onInterestChange(interest, !isSelected) },
                        label = { Text(interest) }
                    )
                }
            }
        }
    }
}

// Bu önizleme, ekranın nasıl göründüğünü gösterir
@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    HeyuTheme {
        // Önizleme için sahte bir ViewModel ve state gerekir,
        // şimdilik sadece ana component'i çağıralım.
        // Gerçek uygulamada bu çalışır.
    }
}
