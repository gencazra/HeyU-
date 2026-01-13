package com.azrag.heyu.ui.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.Role
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
    onNavigateToPrivacy: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = colorScheme.background

    val currentUser by viewModel.currentUser.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val navigateToPrivacy by viewModel.navigateToPrivacy.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(navigateToPrivacy) {
        if (navigateToPrivacy) {
            onNavigateToPrivacy()
            viewModel.resetNavigation()
        }
    }

    // Dil Seçim Diyaloğu
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Dil Seçiniz", color = primaryColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    LanguageOption("Türkçe", currentLanguage == "TR") {
                        viewModel.onLanguageChanged("TR")
                        showLanguageDialog = false
                    }
                    LanguageOption("English", currentLanguage == "EN") {
                        viewModel.onLanguageChanged("EN")
                        showLanguageDialog = false
                    }
                }
            },
            confirmButton = {}
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = primaryColor)
                }
                Text(text = "Profilim/ Ayarlar", fontSize = 14.sp, color = colorScheme.onBackground.copy(alpha = 0.7f))
                Spacer(Modifier.weight(1f))
            }

            Text(text = "heyU!", style = MaterialTheme.typography.displayLarge.copy(fontFamily = LogoFontFamily, fontSize = 48.sp, color = primaryColor))

            Spacer(Modifier.height(16.dp))

            // Profil Bölümü
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                if (!currentUser?.photoUrl.isNullOrEmpty()) {
                    Image(painter = rememberAsyncImagePainter(currentUser?.photoUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = primaryColor)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(text = currentUser?.displayName ?: "Kullanıcı", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colorScheme.onBackground)
            Text(text = currentUser?.email ?: "email@yeditepe.edu.tr", fontSize = 14.sp, color = colorScheme.onBackground.copy(alpha = 0.6f))

            Spacer(Modifier.height(32.dp))

            SettingsItemFilled(icon = Icons.Default.Edit, title = "Profili Düzenle", onClick = onEditProfileClick)

            Spacer(Modifier.height(32.dp))
            Text(text = "Genel Ayarlar", modifier = Modifier.align(Alignment.Start).padding(start = 4.dp), color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(Modifier.height(16.dp))

            SettingsItemFilled(
                icon = Icons.Default.WbSunny,
                title = "Modlar",
                subTitle = "Koyu & Açık",
                hasSwitch = true,
                switchChecked = themeSetting == ThemeSetting.DARK,
                onSwitchChange = { isChecked -> viewModel.onThemeChanged(isChecked) }
            )

            Spacer(Modifier.height(12.dp))

            // Dil Butonu - Artık Seçilebilir
            SettingsItemFilled(
                icon = Icons.Default.Language,
                title = "Dil",
                subTitle = if (currentLanguage == "TR") "Türkçe" else "English",
                onClick = { showLanguageDialog = true }
            )

            Spacer(Modifier.height(12.dp))

            SettingsItemFilled(icon = Icons.Default.Shield, title = "Gizlilik Politikası", onClick = { viewModel.openPrivacyPolicy(context) })

            Spacer(Modifier.height(12.dp))

            SettingsItemFilled(icon = Icons.Default.Star, title = "Uygulamayı Puanla", onClick = { viewModel.rateApp(context) })

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.logout(); onLogout() },
                modifier = Modifier.width(150.dp).height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Çıkış yap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun LanguageOption(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Text(text = text, fontSize = 16.sp)
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
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val surfaceColor = colorScheme.surface

    Box(
        modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(15.dp))
            .background(surfaceColor).clickable(enabled = !hasSwitch) { onClick() }.padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).background(primaryColor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                if (subTitle != null) {
                    Text(text = subTitle, color = colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
            if (hasSwitch) {
                Switch(checked = switchChecked, onCheckedChange = onSwitchChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor))
            } else {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = primaryColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}
