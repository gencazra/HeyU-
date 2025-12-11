package com.azrag.heyu.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.azrag.heyu.R // Varsayılan bir avatar/placeholder drawable olduğundan emin olun
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil ve Ayarlar") },
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
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProfileUiState.Success -> {
                    val user = state.userProfile
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            ProfileHeader(user = user, onEditClick = onNavigateToEditProfile)
                        }
                        item { Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) }
                        item { SettingItem(icon = Icons.Default.Notifications, text = "Bildirim Ayarları", onClick = {}) }
                        item { SettingItem(icon = Icons.Default.ColorLens, text = "Tema (Açık/Koyu)", onClick = {}) }
                        item { SettingItem(icon = Icons.Default.Security, text = "Gizlilik ve Güvenlik", onClick = {}) }
                        item { SettingItem(icon = Icons.AutoMirrored.Filled.HelpOutline, text = "Öneri ve Şikayet", onClick = onNavigateToFeedback) }
                        item { SettingItem(icon = Icons.AutoMirrored.Filled.ExitToApp, text = "Çıkış Yap", isLogout = true, onClick = onLogout) }
                    }
                }
                is ProfileUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

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
                .data(user.avatarUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_launcher_background), // Değiştirin
            error = painterResource(R.drawable.ic_launcher_background),      // Değiştirin
            contentDescription = "Profil Fotoğrafı",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = user.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = user.department, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

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

        Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
            Text("PROFİLİ DÜZENLE")
        }
    }
}
