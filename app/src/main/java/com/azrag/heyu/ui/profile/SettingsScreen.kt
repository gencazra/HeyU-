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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.data.repository.ThemeSetting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

import com.azrag.heyu.ui.theme.LogoFontFamily
import com.azrag.heyu.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

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

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(0.1f)),
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

            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                onClick = onEditProfileClick
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "General Settings",
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp),
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(16.dp))

            SettingsItem(
                icon = Icons.Default.WbSunny,
                title = "Modes",
                subTitle = "Dark & Light",
                hasSwitch = true,
                switchChecked = themeSetting == ThemeSetting.DARK,
                onSwitchChange = { isChecked ->
                    viewModel.onThemeChanged(isChecked)
                }
            )

            SettingsItem(
                icon = Icons.Default.Language,
                title = "Language",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Default.Shield,
                title = "Privacy Policy",
                onClick = { viewModel.openPrivacyPolicy(context) }
            )

            SettingsItem(
                icon = Icons.Default.Star,
                title = "Rate App",
                onClick = { viewModel.rateApp(context) }
            )

            Spacer(Modifier.height(48.dp))

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
                Text("Logout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

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
