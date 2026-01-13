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
import com.azrag.heyu.ui.theme.LogoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val onPrimaryColor = colorScheme.onPrimary
    val backgroundColor = Color(0xFFFDF8F0) // Figma'daki krem rengi arka plan

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
            // Header: Profilim/ Ayarlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = primaryColor)
                }
                Text(
                    text = "Profilim/ Ayarlar",
                    fontSize = 14.sp,
                    color = primaryColor.copy(alpha = 0.7f)
                )
                Spacer(Modifier.weight(1f))
            }

            Text(
                text = "heyU!",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = LogoFontFamily,
                    fontSize = 48.sp,
                    color = primaryColor
                )
            )

            Spacer(Modifier.height(16.dp))

            // Profil Resmi
            Box(
                modifier = Modifier
                    .size(100.dp)
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
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = primaryColor)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = currentUser?.displayName ?: "Kullanıcı Adı",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = primaryColor
            )
            Text(
                text = currentUser?.email ?: "email@yeditepe.edu.tr",
                fontSize = 14.sp,
                color = primaryColor.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(32.dp))

            // Profili Düzenle Butonu (Dolu Stil)
            SettingsItemFilled(
                icon = Icons.Default.Edit,
                title = "Profili Düzenle",
                onClick = onEditProfileClick
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Genel Ayarlar",
                modifier = Modifier.align(Alignment.Start).padding(start = 4.dp),
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            // Ayar Seçenekleri
            SettingsItemFilled(
                icon = Icons.Default.WbSunny,
                title = "Modlar",
                subTitle = "Koyu & Açık",
                hasSwitch = true,
                switchChecked = themeSetting == ThemeSetting.DARK,
                onSwitchChange = { isChecked ->
                    viewModel.onThemeChanged(isChecked)
                }
            )

            Spacer(Modifier.height(12.dp))

            SettingsItemFilled(
                icon = Icons.Default.Language,
                title = "Dil",
                onClick = { /* Dil değiştirme fonksiyonu */ }
            )

            Spacer(Modifier.height(12.dp))

            SettingsItemFilled(
                icon = Icons.Default.Shield,
                title = "Gizlilik Politikası",
                onClick = { viewModel.openPrivacyPolicy(context) }
            )

            Spacer(Modifier.height(12.dp))

            SettingsItemFilled(
                icon = Icons.Default.Star,
                title = "Uygulamayı Puanla",
                onClick = { viewModel.rateApp(context) }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Çıkış Yap Butonu
            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = onPrimaryColor
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Çıkış yap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsItemFilled(
    icon: ImageVector,
    title: String,
    subTitle: String? = null,
    onClick: () -> Unit = {},
    hasSwitch: Boolean = false,
    switchChecked: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White) // Figma'daki kart görünümü için beyaz arka plan
            .clickable(enabled = !hasSwitch) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon dairesi
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = onPrimaryColor, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                if (subTitle != null) {
                    Text(
                        text = subTitle,
                        color = primaryColor.copy(alpha = 0.5f),
                        fontSize = 11.sp
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
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
