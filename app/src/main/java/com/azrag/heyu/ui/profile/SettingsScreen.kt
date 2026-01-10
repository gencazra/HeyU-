package com.azrag.heyu.ui.profile

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.ui.theme.LogoFontFamily
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onAdminPanelClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    
    var userName by remember { mutableStateOf("Kullanıcı") }
    var userEmail by remember { mutableStateOf("") }
    var userPhoto by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            try {
                val doc = Firebase.firestore.collection("users").document(uid).get().await()
                userName = doc.getString("displayName") ?: "Kullanıcı"
                userEmail = doc.getString("email") ?: ""
                userPhoto = doc.getString("photoUrl") ?: ""
            } catch (e: Exception) {}
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = primaryColor)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "heyU!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = LogoFontFamily,
                        fontSize = 42.sp,
                        color = primaryColor
                    )
                )
                Spacer(Modifier.weight(1.3f))
            }

            Spacer(Modifier.height(20.dp))

            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(primaryColor.copy(0.1f)), contentAlignment = Alignment.Center) {
                if (userPhoto.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(userPhoto),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = primaryColor)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(userName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primaryColor)
            Text(userEmail, fontSize = 14.sp, color = onBackgroundColor.copy(0.7f))

            Spacer(Modifier.height(32.dp))

            SettingsItem(icon = Icons.Default.Edit, title = "Profili Editle", onClick = { })
            
            Spacer(Modifier.height(24.dp))
            Text("Genel Ayarlar", modifier = Modifier.align(Alignment.Start).padding(start = 8.dp), color = primaryColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            SettingsItem(icon = Icons.Default.WbSunny, title = "Modlar", subTitle = "Koyu / Açık", hasSwitch = true)
            SettingsItem(icon = Icons.Default.Language, title = "Dil")
            SettingsItem(icon = Icons.Default.Shield, title = "Gizlilik Politikası")
            SettingsItem(icon = Icons.Default.Star, title = "Uygulamayı Puanla")

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    Firebase.auth.signOut()
                    onLogout()
                },
                modifier = Modifier.width(160.dp).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Çıkış yap", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subTitle: String? = null,
    onClick: () -> Unit = {},
    hasSwitch: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(primaryColor, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = onPrimaryColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = onSurfaceColor, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            if (subTitle != null) {
                Text(subTitle, color = onSurfaceColor.copy(0.6f), fontSize = 11.sp)
            }
        }
        if (hasSwitch) {
            Switch(
                checked = false,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = onPrimaryColor,
                    checkedTrackColor = primaryColor,
                    uncheckedThumbColor = primaryColor,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                    uncheckedBorderColor = primaryColor
                )
            )
        } else {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = primaryColor, modifier = Modifier.size(20.dp))
        }
    }
}
