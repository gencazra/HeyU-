package com.azrag.heyu.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = Color(0xFFFDF8F0)

    // Düzenleme Diyaloğu State'leri
    var showEditDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var currentText by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("") } // bio, favori, ilgi, hakkimda

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(dialogTitle, color = primaryColor, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    placeholder = { Text("Bir şeyler yaz...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfileField { profile ->
                            when (editType) {
                                "bio" -> profile.copy(bio = currentText)
                                "favori" -> profile.copy(hobbies = currentText.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                "ilgi" -> profile.copy(interests = currentText.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                "hakkimda" -> profile.copy(aboutTags = currentText.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                else -> profile
                            }
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text("Kaydet", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("İptal", color = primaryColor) }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToSettings) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = primaryColor) }
                Spacer(Modifier.weight(1f))
                Text(text = "heyU!", style = MaterialTheme.typography.displayLarge.copy(fontFamily = LogoFontFamily, fontSize = 42.sp, color = primaryColor))
                Spacer(Modifier.weight(1.3f))
            }

            Spacer(Modifier.height(10.dp))

            // Profil Resimleri
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    if (uiState is MyProfileUiState.Success) {
                        Image(painter = rememberAsyncImagePainter((uiState as MyProfileUiState.Success).profile.photoUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else { Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = primaryColor) }
                }
                Spacer(Modifier.width(16.dp))
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(primaryColor), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Resimleri veya avatarı düzenle", color = primaryColor, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))

            if (uiState is MyProfileUiState.Success) {
                val profile = (uiState as MyProfileUiState.Success).profile

                // ADI
                ProfileEditItem(label = "Adı", value = profile.displayName)
                
                Spacer(Modifier.height(16.dp))
                
                // BIO
                ProfileEditItem(
                    label = "Bio değiştir", 
                    value = profile.bio,
                    hasAdd = true,
                    showValueBelow = true,
                    onClick = { 
                        dialogTitle = "Bio'nu Düzenle"
                        currentText = profile.bio
                        editType = "bio"
                        showEditDialog = true
                    }
                )
                
                Spacer(Modifier.height(16.dp))

                // FAVORİLER
                ProfileEditItem(
                    label = "Favorilerini ekle", 
                    value = profile.hobbies.joinToString("  ") { "*$it" }, 
                    hasAdd = true,
                    showValueBelow = true,
                    onClick = { 
                        dialogTitle = "Favorilerini Düzenle (Virgülle ayır)"
                        currentText = profile.hobbies.joinToString(", ")
                        editType = "favori"
                        showEditDialog = true
                    }
                )

                Spacer(Modifier.height(16.dp))

                // İLGİ ALANLARI
                ProfileEditItem(
                    label = "İlgi alanlarını ekle", 
                    value = profile.interests.joinToString("  ") { "*$it" }, 
                    hasAdd = true,
                    showValueBelow = true,
                    onClick = { 
                        dialogTitle = "İlgi Alanlarını Düzenle (Virgülle ayır)"
                        currentText = profile.interests.joinToString(", ")
                        editType = "ilgi"
                        showEditDialog = true
                    }
                )

                Spacer(Modifier.height(16.dp))

                // HAKKIMDA
                ProfileEditItem(
                    label = "Hakkımda kısmı ekle", 
                    value = profile.aboutTags.joinToString("  ") { "*$it" }, 
                    hasAdd = true,
                    showValueBelow = true,
                    onClick = { 
                        dialogTitle = "Hakkında Düzenle (Virgülle ayır)"
                        currentText = profile.aboutTags.joinToString(", ")
                        editType = "hakkimda"
                        showEditDialog = true
                    }
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileEditItem(
    label: String, 
    value: String, 
    hasAdd: Boolean = false,
    showValueBelow: Boolean = false,
    onClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = primaryColor, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(55.dp).clip(RoundedCornerShape(15.dp)).background(primaryColor).clickable { onClick() }.padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (showValueBelow) "" else value, color = Color.White, fontSize = 15.sp)
                if (hasAdd) Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
        if (showValueBelow && value.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(text = value, color = primaryColor, fontSize = 15.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
