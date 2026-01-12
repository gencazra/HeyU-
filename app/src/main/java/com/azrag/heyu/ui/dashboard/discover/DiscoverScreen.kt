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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
    val colorScheme = MaterialTheme.colorScheme

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(colorScheme.secondary, colorScheme.primary)
    )

    LaunchedEffect(uiState.newMatch) {
        uiState.newMatch?.let { matchedUser ->
            mainNavController.navigate(Screen.MatchSuccess.route.replace("{matchedUserId}", matchedUser.id))
            viewModel.clearMatch()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "heyU!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = LogoFontFamily,
                        fontSize = 32.sp,
                        color = colorScheme.onPrimary
                    )
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = colorScheme.onPrimary)
                } else if (uiState.userCards.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No one new around you!", 
                            color = colorScheme.onPrimary, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.onPrimary, contentColor = colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Refresh")
                        }
                    }
                } else {
                    uiState.userCards.asReversed().forEach { user ->
                        SwipeableCard(
                            user = user,
                            onSwiped = { liked ->
                                viewModel.onCardSwiped(user, liked)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
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
    val swipeThreshold = screenWidth * 0.4f
    val colorScheme = MaterialTheme.colorScheme

    val offsetX = remember { Animatable(0f) }
    val rotation = (offsetX.value / 20f)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer { rotationZ = rotation }
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
                            scope.launch { offsetX.animateTo(0f, spring()) }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    }
                )
            }
            .fillMaxWidth(0.85f)
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(32.dp))
            .background(colorScheme.surface)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(user.photoUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${user.displayName}, ${user.age}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Text(
                    text = user.department,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    user.hobbies.take(3).forEach { hobby ->
                        Surface(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = hobby,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
