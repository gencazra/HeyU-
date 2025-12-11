// Dosya: ui/profile/EditProfileScreen.kt

package com.azrag.heyu.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel, // Bu ekran da aynı ViewModel'i kullanacak
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var bio by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") } // Virgülle ayrılmış metin olarak
    var isLoading by remember { mutableStateOf(false) }

    // Bu `LaunchedEffect`, ekran açıldığında ve uiState başarıyla yüklendiğinde
    // ViewModel'den gelen verilerle TextField'ları sadece bir kez doldurur.
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val user = (uiState as ProfileUiState.Success).userProfile
            bio = user.bio
            interests = user.interests.joinToString(", ") // Listeyi virgüllü metne çevir
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profili Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProfileUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Bio alanı
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Kısa Bio") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // İlgi Alanları alanı
                        OutlinedTextField(
                            value = interests,
                            onValueChange = { interests = it },
                            label = { Text("İlgi Alanları (virgülle ayırarak)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        // Kaydet butonu
                        Button(
                            onClick = {
                                isLoading = true
                                // Virgüllü metni tekrar listeye çeviriyoruz
                                val interestsList = interests.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                                viewModel.updateUserProfile(
                                    newBio = bio,
                                    newInterests = interestsList,
                                    onSuccess = {
                                        Toast.makeText(context, "Profil güncellendi!", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                        onNavigateBack() // Başarılı olunca bir önceki ekrana dön
                                    },
                                    onError = { errorMessage ->
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        isLoading = false
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("KAYDET")
                            }
                        }
                    }
                }
                is ProfileUiState.Error -> {
                    Text(
                        text = (uiState as ProfileUiState.Error).message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
