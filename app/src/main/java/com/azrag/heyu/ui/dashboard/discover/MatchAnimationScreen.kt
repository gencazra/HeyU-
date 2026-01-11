package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.ui.theme.LogoFontFamily
import com.azrag.heyu.util.Result
import com.azrag.heyu.util.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    var matchedUser by remember { mutableStateOf<UserProfile?>(null) }
    var showMatchContent by remember { mutableStateOf(true) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    LaunchedEffect(matchedUserId) {
        if (matchedUserId != null) {
            val result = viewModel.userRepository.getUserProfile(matchedUserId)
            if (result is Result.Success<UserProfile?>) { matchedUser = result.data }
        }
        delay(3000)
        showMatchContent = false
        delay(500)
        navController.navigate(Screen.MessageList.route) {
            popUpTo(Screen.Dashboard.route) { inclusive = false }
        }
    }

    val currentUser = Firebase.auth.currentUser

    AnimatedVisibility(
        visible = showMatchContent,
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 100.dp)
                    .scale(1.5f)
                    .background(primaryColor, CircleShape)
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))
                Text(
                    text = "heyU!",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = LogoFontFamily,
                        fontSize = 42.sp,
                        color = primaryColor
                    )
                )

                Spacer(Modifier.height(40.dp))
                Text(
                    text = "Hi, ${currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Selin"}!",
                    fontSize = 18.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(40.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Image(
                            painter = rememberAsyncImagePainter(currentUser?.photoUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width((-20).dp))
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(backgroundColor).padding(4.dp)) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Image(
                                painter = rememberAsyncImagePainter(matchedUser?.photoUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
                Text(
                    text = "${matchedUser?.displayName ?: "Someone"} \nis now your friend!",
                    color = onPrimaryColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.heyu_logo),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                    Text("hey!", color = onPrimaryColor, fontWeight = FontWeight.Bold)
                    Text("send", color = onPrimaryColor, fontSize = 12.sp)
                }
                Spacer(Modifier.height(60.dp))
            }
        }
    }
}
