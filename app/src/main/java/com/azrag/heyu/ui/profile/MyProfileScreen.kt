package com.azrag.heyu.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is MyProfileUiState.LoggedOut) onLogoutSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilim") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = uiState) {
                is MyProfileUiState.Loading -> CircularProgressIndicator()
                is MyProfileUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is MyProfileUiState.Success -> {
                    ProfileContent(profile = state.profile, onEditClick = onNavigateToEditProfile, onLogoutClick = { viewModel.logout() })
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ProfileContent(profile: UserProfile, onEditClick: () -> Unit, onLogoutClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = rememberAsyncImagePainter(profile.photoUrl.ifEmpty { R.drawable.ic_default_profile }),
            contentDescription = null,
            modifier = Modifier.size(140.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(16.dp))
        Text(profile.displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(profile.department, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))
        Text(profile.bio, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Edit, null)
            Spacer(Modifier.width(8.dp))
            Text("PROFİLİ DÜZENLE")
        }
        TextButton(onClick = onLogoutClick) { Text("Çıkış Yap", color = MaterialTheme.colorScheme.error) }
    }
}
