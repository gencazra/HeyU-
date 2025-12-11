package com.azrag.heyu.ui.profile // Paket adının bu olduğundan emin ol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.azrag.heyu.R // Kendi R dosyanın yolu
import androidx.hilt.navigation.compose.hiltViewModel
import com.azrag.heyu.ui.theme.HeyuTheme
import com.google.accompanist.flowlayout.FlowRow

// Bu fonksiyon NavHost içinde çağrılacak ve navigasyonu yönetecek.
@Composable
fun MyProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    // ViewModel'den gelen anlık state'i dinle
    val uiState by viewModel.uiState.collectAsState()

    // Gelen state'e göre hangi arayüzün gösterileceğini yönet
    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            // Yükleniyor durumunda ekranın ortasında bir progress indicator göster
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.Success -> {
            // Başarılı durumda asıl profil içeriğini göster
            MyProfileSuccessContent(
                userProfile = state.userProfile,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onNavigateToSettings = onNavigateToSettings,
                onLogout = {
                    viewModel.logout()
                    onLogoutSuccess() // Navigasyonu tetikle
                }
            )
        }
        is ProfileUiState.Error -> {
            // Hata durumunda bir hata mesajı göster
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Hata: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// Bu Composable, sadece veri başarıyla yüklendiğinde gösterilecek olan asıl UI'dır.
// Kodu bu şekilde ayırmak, hem okunabilirliği artırır hem de önizlemeyi kolaylaştırır.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MyProfileSuccessContent(
    userProfile: UserProfile,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilim", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Bilgi Kartı
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = userProfile.avatarUrl.ifEmpty { R.drawable.ic_launcher_background }, // Varsayılan bir resim ata
                    contentDescription = "Profil Fotoğrafı",
                    modifier = Modifier.size(100.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = userProfile.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = userProfile.department, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userProfile.bio.ifEmpty { "Henüz bir bio eklenmemiş." },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // İlgi Alanları
            item {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    userProfile.interests.forEach { interest ->
                        SuggestionChip(onClick = {}, label = { Text(interest) }, modifier = Modifier.padding(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // "Profili Düzenle" Butonu
            item {
                Button(
                    onClick = onNavigateToEditProfile,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text("Profili Düzenle")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Ayarlar Menüsü
            item {
                ProfileMenuItem(title = "Bildirim Ayarları", onClick = {})
                ProfileMenuItem(title = "Tema (Açık/Koyu)", onClick = {})
                ProfileMenuItem(title = "Gizlilik ve Güvenlik", onClick = {})
                ProfileMenuItem(title = "Öneri ve Şikayet", onClick = {})
                ProfileMenuItem(title = "Çıkış Yap", isDestructive = true, onClick = onLogout)
            }
        }
    }
}

// Menü elemanları için standart bir Composable
@Composable
private fun ProfileMenuItem(title: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
        HorizontalDivider()
    }
}


// Bu @Preview bloğu sayesinde, ekranın nasıl görüneceğini uygulamayı çalıştırmadan görebilirsin.
// Bu bloğun kırmızılığı GİTMELİDİR.
@Preview(showBackground = true)
@Composable
fun MyProfileSuccessContentPreview() {
    HeyuTheme {
        MyProfileSuccessContent(
            userProfile = UserProfile(
                fullName = "Azra G. (Preview)",
                department = "Bilgisayar Mühendisliği",
                bio = "Bu bir önizleme ekranıdır. Harika çalışıyor!",
                interests = listOf("#compose", "#android", "#kotlin", "#uiux", "#firebase", "#vintage")
            ),
            onNavigateToEditProfile = {},
            onNavigateToSettings = {},
            onLogout = {}
        )
    }
}

