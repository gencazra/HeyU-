package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.azrag.heyu.util.Screen
import kotlinx.coroutines.delay

@Composable
fun DiscoverScreen(
    mainNavController: NavController,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    // Tema renklerini MaterialTheme üzerinden alıyoruz
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var showHeyU by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 80.dp, bottomEnd = 80.dp))
                        .background(primaryColor) // Sabit turuncu yerine tema ana rengi
                )

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp))
                        Text(
                            text = "heyU!",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = LogoFontFamily,
                                fontSize = 42.sp,
                                color = Color.White, // Logo genellikle sabit kalabilir veya onPrimary olabilir
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        IconButton(onClick = { mainNavController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, null, tint = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = "https://via.placeholder.com/150"),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Elif Ertürk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = primaryColor
                )
                Text(
                    text = "22, VCD",
                    fontSize = 14.sp,
                    color = primaryColor.copy(0.7f)
                )
                Text(
                    text = "\"Dursun Zaman\"",
                    fontSize = 14.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        showHeyU = true
                    },
                    modifier = Modifier.width(180.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mesaj At", fontWeight = FontWeight.Bold)
                }
            }
        }

        AnimatedVisibility(
            visible = showHeyU,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "heyU!",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = LogoFontFamily,
                            fontSize = 80.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    LaunchedEffect(Unit) {
                        delay(2000)
                        showHeyU = false
                        mainNavController.navigate(Screen.MessageList.route)
                    }
                }
            }
        }
    }
}