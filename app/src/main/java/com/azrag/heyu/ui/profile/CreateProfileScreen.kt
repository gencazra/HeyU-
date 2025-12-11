// Dosya: ui/profile/CreateProfileScreen.kt (SON HALİ)

package com.azrag.heyu.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.azrag.heyu.ui.theme.HeyUTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(
    fullName: String,
    onProfileCreated: () -> Unit,
    viewModel: CreateProfileViewModel = hiltViewModel() // ViewModel'i Hilt ile alıyoruz
) {
    val context = LocalContext.current
    var department by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profilini Oluştur") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("Merhaba, $fullName!", style = MaterialTheme.typography.headlineMedium)
                Text("Birkaç bilgi daha ekleyelim.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Bölümün") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Kısa Bio") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = interests,
                    onValueChange = { interests = it },
                    label = { Text("İlgi Alanları (virgülle ayırarak)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (department.isBlank() || bio.isBlank()) {
                            Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        viewModel.saveProfile(
                            fullName = fullName,
                            department = department,
                            bio = bio,
                            interests = interests,
                            onSuccess = {
                                // Başarılı olduğunda ana ekrana git
                                onProfileCreated()
                            },
                            onError = { errorMessage ->
                                // Hata olduğunda kullanıcıya göster ve yüklemeyi durdur
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                isLoading = false
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("KAYDET VE DEVAM ET")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateProfileScreenPreview() {
    HeyUTheme {
        CreateProfileScreen(fullName = "Azra G.", onProfileCreated = {})
    }
}
