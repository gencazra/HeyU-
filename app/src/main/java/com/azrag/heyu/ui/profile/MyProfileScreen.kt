package com.azrag.heyu.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.ui.theme.LogoFontFamily

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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Geri */ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "heyU!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = LogoFontFamily,
                        fontSize = 42.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState is MyProfileUiState.Success) {
                        val profile = (uiState as MyProfileUiState.Success).profile
                        if (profile.photoUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(profile.photoUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(70.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Resimleri veya avatarı düzenle", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))

            if (uiState is MyProfileUiState.Success) {
                val profile = (uiState as MyProfileUiState.Success).profile
                
                ProfileDisplayItem(label = "Adı", value = profile.displayName)
                Spacer(Modifier.height(12.dp))
                ProfileDisplayItem(label = "Bio değiştir", value = profile.bio)
                Spacer(Modifier.height(12.dp))
                ProfileDisplayItem(label = "Favorilerini ekle", value = "*Film  *Dizi  *Müzik", hasAdd = true)
                Spacer(Modifier.height(12.dp))
                ProfileDisplayItem(label = "İlgi alanlarını ekle", value = "*Kokteyl  *Yüzme  *Yoga", hasAdd = true)
                Spacer(Modifier.height(12.dp))
                ProfileDisplayItem(label = "Hakkında kısmını ekle", value = "*Vegan  *Tatlıya düşkün  *Hayvansever", hasAdd = true)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileDisplayItem(label: String, value: String, hasAdd: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label, 
            color = MaterialTheme.colorScheme.primary, 
            fontSize = 14.sp, 
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = value, color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                if (hasAdd) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
