package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.azrag.heyu.R
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.ui.theme.LogoFontFamily
import com.azrag.heyu.util.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    val userRepository: UserRepository
) : ViewModel()

@Composable
fun MatchAnimationScreen(
    navController: NavController,
    matchedUserId: String?,
    viewModel: MatchViewModel = hiltViewModel()
) {
    var showMatchContent by remember { mutableStateOf(true) }
    
    val colorScheme = MaterialTheme.colorScheme
    
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colorScheme.secondary,
            colorScheme.primary
        )
    )

    val contentColor = colorScheme.onBackground

    LaunchedEffect(Unit) {
        delay(3500)
        showMatchContent = false
        delay(500)
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    AnimatedVisibility(
        visible = showMatchContent,
        enter = fadeIn(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "heyU!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = LogoFontFamily,
                        fontSize = 32.sp,
                        color = contentColor
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "hey!\nwhile chatting",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.heyu_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(20.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "don't forget\nto be polite!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
