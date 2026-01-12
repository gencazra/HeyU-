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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.data.repository.ThemeSetting
import com.azrag.heyu.ui.dashboard.discover.DiscoverViewModel
import com.azrag.heyu.ui.theme.LogoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    onNavigateToEvents: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    discoverViewModel: DiscoverViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = colorScheme.background
    val onBackgroundColor = colorScheme.onBackground

    val currentUser by viewModel.currentUser.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            // Profil Resmi Bölümü
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!currentUser?.photoUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(currentUser?.photoUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(70.dp), tint = primaryColor)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = currentUser?.displayName ?: "User",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = primaryColor
            )
            Text(
                text = currentUser?.email ?: "email@yeditepe.edu.tr",
                fontSize = 14.sp,
                color = onBackgroundColor.copy(0.6f)
            )

            Spacer(Modifier.height(32.dp))

            // Test User Ekleme (Debug)
            Button(
                onClick = { 
                    discoverViewModel.add10TestUsers {
                        Toast.makeText(context, "10 Test Kullanıcısı Eklendi!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("DEBUG: 10 Test User Ekle", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Profili Editle",
                onClick = onEditProfileClick
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Genel Ayarlar",
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp),
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            SettingsItem(
                icon = Icons.Default.WbSunny,
                title = "Modlar",
                subTitle = "Koyu & Açık",
                hasSwitch = true,
                switchChecked = themeSetting == ThemeSetting.DARK,
                onSwitchChange = { isChecked ->
                    viewModel.onThemeChanged(isChecked)
                }
            )

            SettingsItem(
                icon = Icons.Default.Language,
                title = "Dil",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Default.Shield,
                title = "Gizlilik Politikası",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Default.Star,
                title = "Uygulamayı Puanla",
                onClick = { }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .width(180.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Çıkış yap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subTitle: String? = null,
    onClick: () -> Unit = {},
    hasSwitch: Boolean = false,
    switchChecked: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val onSurfaceColor = colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !hasSwitch) { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(primaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = onSurfaceColor,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp
            )
            if (subTitle != null) {
                Text(
                    text = subTitle,
                    color = onSurfaceColor.copy(0.5f),
                    fontSize = 12.sp
                )
            }
        }

        if (hasSwitch) {
            Switch(
                checked = switchChecked,
                onCheckedChange = onSwitchChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor,
                    uncheckedThumbColor = primaryColor,
                    uncheckedTrackColor = Color.LightGray,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        } else {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
