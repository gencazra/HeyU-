package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.azrag.heyu.ui.theme.LogoFontFamily
import com.azrag.heyu.util.Screen
import kotlinx.coroutines.delay

@Composable
fun MatchAnimationScreen(
    navController: NavController,
    matchedUserId: String?
) {
    val orangeColor = Color(0xFFE67E59)
    val yellowColor = Color(0xFFFFD54F)
    val beigeColor = Color(0xFFFFF8E1)

    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(4000)
        navController.popBackStack() // Discover'a geri dön
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(yellowColor)
    ) {
        // Arka plandaki turuncu kavis (Figma'daki gibi)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 100.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.4f)
                    quadraticBezierTo(
                        size.width * 0.5f, size.height * 0.2f,
                        size.width, size.height * 0.4f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, orangeColor)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "heyU!",
                fontFamily = LogoFontFamily,
                fontSize = 40.sp,
                color = orangeColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "hey!\nsohbet ederken",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = orangeColor,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Kalp Animasyonu
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                
                // Kalbin zıplama animasyonu
                val bounce by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                // Kalp Çizimi (Canvas ile)
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer { translationY = bounce }
                ) {
                    val width = size.width
                    val height = size.height
                    val path = Path().apply {
                        moveTo(width / 2, height / 4)
                        cubicTo(width / 4, 0f, 0f, height / 4, 0f, height / 2)
                        cubicTo(0f, height * 3 / 4, width / 4, height, width / 2, height)
                        cubicTo(width * 3 / 4, height, width, height * 3 / 4, width, height / 2)
                        cubicTo(width, height / 4, width * 3 / 4, 0f, width / 2, height / 4)
                    }
                    drawPath(path, orangeColor)
                }
                
                // Kalbin gözleri ve ağzı (Figma'daki gibi sempatik bir karakter)
                Column(
                    modifier = Modifier.graphicsLayer { translationY = bounce - 10f },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        Box(modifier = Modifier.size(12.dp).background(Color.White, CircleShape))
                        Spacer(modifier = Modifier.width(24.dp))
                        Box(modifier = Modifier.size(12.dp).background(Color.White, CircleShape))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.size(30.dp, 4.dp).background(Color.White, CircleShape))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "kibar olmayı\nunutma!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
