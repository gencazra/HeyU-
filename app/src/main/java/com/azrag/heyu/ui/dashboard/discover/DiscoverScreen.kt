package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.ui.theme.LogoFontFamily
import com.azrag.heyu.data.model.UserProfile

@Composable
fun DiscoverScreen(
    mainNavController: NavController,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    
    val uiState by viewModel.uiState.collectAsState()
    
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentProfile = uiState.userCards.getOrNull(currentIndex)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "heyU!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = LogoFontFamily,
                        fontSize = 42.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "hey, Selin!", 
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (currentProfile != null) {
                ProfileCard(profile = currentProfile, primaryColor = primaryColor)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage ?: "Yeni aday bulunamadı.", color = primaryColor)
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: UserProfile, primaryColor: Color) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 150.dp, topEnd = 30.dp, bottomStart = 30.dp, bottomEnd = 150.dp))
                    .background(primaryColor)
            )

            Image(
                painter = rememberAsyncImagePainter(model = profile.photoUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profile.displayName,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            color = primaryColor
        )
        Text(
            text = "${profile.age}, ${profile.department}",
            fontSize = 18.sp,
            color = primaryColor.copy(0.7f)
        )
        Text(
            text = "\"${profile.bio}\"",
            fontSize = 14.sp,
            color = primaryColor,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton(text = "Favorileri", isSelected = selectedTab == 0, primaryColor = primaryColor, onClick = { selectedTab = 0 })
            TabButton(text = "İlgi Alanları", isSelected = selectedTab == 1, primaryColor = primaryColor, onClick = { selectedTab = 1 })
            TabButton(text = "Hakkında", isSelected = selectedTab == 2, primaryColor = primaryColor, onClick = { selectedTab = 2 })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            when (selectedTab) {
                0 -> {
                    profile.hobbies.forEach { hobby ->
                        Text(text = "* $hobby", color = primaryColor, fontSize = 18.sp)
                    }
                }
                1 -> {
                   Text(text = "İlgi alanları listesi...", color = primaryColor, fontSize = 18.sp)
                }
                2 -> {
                    Text(text = profile.bio, color = primaryColor, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, primaryColor: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFFC7B8F5) else Color.Transparent, 
        border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.Black else primaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
