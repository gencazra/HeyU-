package com.azrag.heyu.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azrag.heyu.R
import com.azrag.heyu.ui.theme.LogoFontFamily
import com.azrag.heyu.util.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsState()
    
    var showHey by remember { mutableStateOf(false) }
    var showU by remember { mutableStateOf(false) }
    var showFullLogo by remember { mutableStateOf(false) }


    val uAlpha by animateFloatAsState(
        targetValue = if (showU) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "UAlpha"
    )
    
    val uScale by animateFloatAsState(
        targetValue = if (showU) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "UScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (showFullLogo) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "LogoAlpha"
    )

    LaunchedEffect(Unit) {
        delay(500)
        showHey = true
        delay(800)
        showU = true
        delay(1500)
        showFullLogo = true
        delay(2000)
        
        startDestination?.let { destination ->
            navController.navigate(destination) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE082)),
        contentAlignment = Alignment.Center
    ) {
        if (!showFullLogo) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showHey) {
                    Text(
                        text = "hey",
                        fontSize = 72.sp,
                        fontFamily = LogoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE57373)
                    )
                }
                if (showU) {
                    Text(
                        text = "u!",
                        fontSize = 84.sp,
                        fontFamily = LogoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE57373),
                        modifier = Modifier
                            .alpha(uAlpha)
                            .scale(uScale)
                            .padding(start = 4.dp)
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.heyu_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(280.dp)
                    .alpha(logoAlpha)
            )
        }
    }
}
