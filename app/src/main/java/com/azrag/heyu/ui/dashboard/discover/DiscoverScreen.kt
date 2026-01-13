package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.ui.theme.LogoFontFamily
import com.azrag.heyu.util.Screen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DiscoverScreen(
    mainNavController: NavController,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = Color(0xFFFFF8E1)

    LaunchedEffect(uiState.newMatch) {
        uiState.newMatch?.let { matchedUser ->
            mainNavController.navigate(Screen.MatchSuccess.route.replace("{matchedUserId}", matchedUser.id))
            viewModel.clearMatch()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Logo / Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "heyU!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = LogoFontFamily,
                        fontSize = 40.sp,
                        color = Color(0xFFE67E59)
                    )
                )
            }

            // Cards Area
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color(0xFFE67E59))
                } else if (uiState.errorMessage != null) {
                    // Hata mesajını ekranda göster
                    ErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.refresh() }
                    )
                } else if (uiState.userCards.isEmpty()) {
                    EmptyState(onRefresh = { viewModel.add10TestUsers() })
                } else {
                    uiState.userCards.asReversed().forEach { user ->
                        key(user.id) {
                            SwipeableCard(
                                user = user,
                                onSwiped = { liked ->
                                    viewModel.onCardSwiped(user, liked)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "Bir hata oluştu",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E59))
        ) {
            Text("Tekrar Dene")
        }
    }
}

@Composable
fun SwipeableCard(
    user: UserProfile,
    onSwiped: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val swipeThreshold = screenWidth * 0.45f

    val offsetX = remember { Animatable(0f) }
    val rotation = (offsetX.value / 25f)
    val scope = rememberCoroutineScope()

    val orangeColor = Color(0xFFE67E59)
    val beigeColor = Color(0xFFFFF8E1)

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = rotation
                val scale = (1f - (kotlin.math.abs(offsetX.value) / (screenWidth.toPx() * 3f))).coerceIn(0.9f, 1f)
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX.value > swipeThreshold.toPx()) {
                            scope.launch {
                                offsetX.animateTo(screenWidth.toPx() * 2, tween(300))
                                onSwiped(true)
                            }
                        } else if (offsetX.value < -swipeThreshold.toPx()) {
                            scope.launch {
                                offsetX.animateTo(-screenWidth.toPx() * 2, tween(300))
                                onSwiped(false)
                            }
                        } else {
                            scope.launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    }
                )
            }
            .fillMaxWidth(0.88f)
            .fillMaxHeight(0.82f)
            .clip(RoundedCornerShape(36.dp))
            .background(orangeColor)
    ) {
        // Wave Effect
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    color = beigeColor,
                    shape = RoundedCornerShape(topStart = 160.dp)
                )
        )

        Text(
            text = "hey, ${user.displayName.split(" ").first()} !",
            color = beigeColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = LogoFontFamily,
            modifier = Modifier.padding(top = 48.dp, start = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(user.photoUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 12.dp)
                        .size(44.dp),
                    shape = CircleShape,
                    color = beigeColor,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✦",
                            fontSize = 24.sp,
                            color = orangeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = user.displayName,
                color = orangeColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${user.age}, ${user.department}",
                color = orangeColor.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "*Derssiz Zaman",
                color = orangeColor.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoPill("Favoriler")
                InfoPill("İlgi Alanları")
                InfoPill("Hakkında")
            }
        }
    }
}

@Composable
fun InfoPill(text: String) {
    Surface(
        color = Color(0xFFE67E59).copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color(0xFFE67E59),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyState(onRefresh: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            "Etrafında yeni kimse yok!",
            color = Color(0xFFE67E59),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFE67E59), CircleShape)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Yenile", tint = Color.White)
        }
    }
}
