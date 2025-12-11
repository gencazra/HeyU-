// Dosya: ui/profile/MyProfileScreen.kt

package com.azrag.heyu.ui.profile

import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.preference.isNotEmpty
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.azrag.heyu.R // Varsayılan bir placeholder drawable olduğundan emin olun
import com.google.accompanist.flowlayout.FlowRow

// Bu Composable'ı SettingsScreen'den ayrı tutuyoruz ki gelecekte farklılaşabilir.
@Composable
fun MyProfileScreen(
    viewModel: ProfileViewModel, // NavGraph'ten bu viewModel'i alacağız
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    // ViewModel'deki UI durumunu (state) dinliyoruz.
    // uiState her değiştiğinde (Loading -> Success gibi), bu ekran yeniden çizilir.
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                // Durum "Yükleniyor" ise ekranın ortasında bir progress indicator göster
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Success -> {
                // Durum "Başarılı" ise gelen kullanıcı verileriyle ekranı doldur
                val user = state.userProfile
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Parça: Profilin üst kısmı (Foto, İsim, Bio vb.)
                    item {
                        ProfileHeader(user = user, onEditClick = onNavigateToEditProfile)
                    }

                    // 2. Parça: Ayarlar ve Çıkış Yap butonları
                    item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }
                    item {
                        // Ayarlar ekranına gitmek için bir navigasyon butonu
                        SettingItem(
                            icon = Icons.Default.Settings,
                            text = "Uygulama Ayarları",
                            onClick = onNavigateToSettings
                        )
                    }
                    item {
                        // Çıkış yapmak için buton
                        SettingItem(
                            icon = Icons.Default.Logout,
                            text = "Çıkış Yap",
                            isLogout = true,
                            onClick = onLogoutSuccess
                        )
                    }
                }
            }
            is ProfileUiState.Error -> {
                // Durum "Hata" ise ekranın ortasında hata mesajını göster
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Bu yardımcı Composable, hem MyProfileScreen hem de SettingsScreen tarafından kullanılabilir.
// Henüz oluşturmadıysak bu kodu da ekleyelim.
@Composable
fun ProfileHeader(user: UserProfile, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.profileImageUrl.ifEmpty { R.drawable.ic_launcher_background }) // URL boşsa varsayılanı kullan
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_launcher_background), // Yüklenirken gösterilecek
            error = painterResource(R.drawable.ic_launcher_background), // Hata olursa gösterilecek
            contentDescription = "Profil Fotoğrafı",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = user.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = user.department, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = user.bio, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // İlgi alanları etiketlerini göster
        if (user.interests.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                user.interests.forEach { interest ->
                    SuggestionChip(onClick = {}, label = { Text(interest) })
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(onClick = onEditClick) {
            Text("PROFİLİ DÜZENLE")
        }
    }
}
