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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val characterColor = Color(0xFFE67E59)

    LaunchedEffect(Unit) {
        delay(4000)
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(yellowColor)
    ) {
        // Figma Wave Effect (Orange bottom area)
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    quadraticBezierTo(
                        size.width * 0.4f, size.height * 0.55f,
                        size.width, size.height * 0.75f
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
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A), // Figma text color
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Character Animation Box
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "heartTransition")
                
                // Bouncing and scale animation
                val bounce by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bounce"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = size.width / 2
                    val top = size.height / 2 + bounce
                    val heartSize = 120f

                    // 1. Draw Legs
                    val leftLegPath = Path().apply {
                        moveTo(center - 30f, top + 60f)
                        quadraticBezierTo(center - 50f, top + 100f, center - 40f, top + 130f)
                    }
                    drawPath(leftLegPath, Color.Black, style = Stroke(width = 6f, cap = StrokeCap.Round))
                    
                    val rightLegPath = Path().apply {
                        moveTo(center + 30f, top + 60f)
                        quadraticBezierTo(center + 50f, top + 100f, center + 60f, top + 130f)
                    }
                    drawPath(rightLegPath, Color.Black, style = Stroke(width = 6f, cap = StrokeCap.Round))

                    // 2. Draw Main Heart Body
                    val heartPath = Path().apply {
                        moveTo(center, top + heartSize / 4)
                        cubicTo(center - heartSize / 2, top - heartSize / 2, center - heartSize, top + heartSize / 4, center, top + heartSize)
                        cubicTo(center + heartSize, top + heartSize / 4, center + heartSize / 2, top - heartSize / 2, center, top + heartSize / 4)
                    }
                    drawPath(heartPath, characterColor)

                    // 3. Draw Arms (Holding head/heart top like in Figma)
                    val leftArmPath = Path().apply {
                        moveTo(center - heartSize * 0.7f, top + 30f)
                        quadraticBezierTo(center - heartSize * 0.9f, top - 20f, center - 30f, top - 10f)
                    }
                    drawPath(leftArmPath, Color.Black, style = Stroke(width = 5f, cap = StrokeCap.Round))

                    val rightArmPath = Path().apply {
                        moveTo(center + heartSize * 0.7f, top + 30f)
                        quadraticBezierTo(center + heartSize * 0.9f, top - 20f, center + 30f, top - 10f)
                    }
                    drawPath(rightArmPath, Color.Black, style = Stroke(width = 5f, cap = StrokeCap.Round))

                    // 4. Draw Face
                    // Eyes
                    drawCircle(Color.Black, radius = 4f, center = androidx.compose.ui.geometry.Offset(center - 25f, top + 35f))
                    drawCircle(Color.Black, radius = 4f, center = androidx.compose.ui.geometry.Offset(center + 25f, top + 35f))
                    
                    // Mouth
                    val mouthPath = Path().apply {
                        moveTo(center - 10f, top + 55f)
                        quadraticBezierTo(center, top + 62f, center + 10f, top + 55f)
                    }
                    drawPath(mouthPath, Color.Black, style = Stroke(width = 3f, cap = StrokeCap.Round))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "kibar olmayı\nunutma!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F), // Figma reddish text
                textAlign = TextAlign.Center
            )
        }
    }
}
