package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.azrag.heyu.data.model.UserProfile
import com.azrag.heyu.data.repository.ChatRepository
import com.azrag.heyu.data.repository.UserRepository
import com.azrag.heyu.util.Result
import com.azrag.heyu.util.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 1. VIEWMODEL: UserRepository ve ChatRepository'yi ekrana bağlar
@HiltViewModel
class MatchViewModel @Inject constructor(
    val userRepository: UserRepository,
    val chatRepository: ChatRepository
) : ViewModel()

@Composable
fun MatchAnimationScreen(
    navController: NavController,
    matchedUserId: String?,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser

    // 2. STATE YÖNETİMİ: 'getUserProfileStream' veya 'getCurrentUserProfile' ile uyumlu mühürleme
    val userState: State<Result<UserProfile?>> = produceState<Result<UserProfile?>>(
        initialValue = Result.Loading,
        key1 = matchedUserId
    ) {
        if (matchedUserId == null) {
            value = Result.Error("Kullanıcı bulunamadı.")
        } else {
            val result = withContext(Dispatchers.IO) {
                // Not: UserRepository içinde 'getUserProfileStream' varsa onu,
                // tek seferlik 'getCurrentUserProfile' varsa onu çağırın.
                // Burada genel 'getCurrentUserProfile' referans alınmıştır.
                viewModel.userRepository.getCurrentUserProfile()
            }
            value = result
        }
    }

    var isCreatingChat by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // TİP ÇIKARIMI HATASI ÇÖZÜMÜ: Tipi açıkça belirtiyoruz
        val currentState: Result<UserProfile?> = userState.value

        when (currentState) {
            is Result.Loading -> {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f))) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                }
            }
            is Result.Success -> {
                val matchedUser = currentState.data
                if (matchedUser != null) {
                    AnimatedMatchContent(
                        currentUserPhoto = currentUser?.photoUrl?.toString(),
                        matchedUser = matchedUser,
                        isCreatingChat = isCreatingChat,
                        onNavigateToChat = {
                            coroutineScope.launch {
                                isCreatingChat = true
                                when (val chatResult = viewModel.chatRepository.createOrGetChatRoom(matchedUser.id)) {
                                    is Result.Success -> {
                                        val cid = chatResult.data ?: ""
                                        navController.navigate(Screen.Chat.createRoute(cid)) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                                        }
                                    }
                                    else -> { isCreatingChat = false }
                                }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }
            is Result.Error -> {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
            else -> { /* Exhaustive else */ }
        }
    }
}

@Composable
private fun AnimatedMatchContent(
    currentUserPhoto: String?,
    matchedUser: UserProfile,
    isCreatingChat: Boolean,
    onNavigateToChat: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )

    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (startAnimation) 1f else 0.5f, spring(Spring.DampingRatioMediumBouncy), label = "scale")
    val alpha by animateFloatAsState(if (startAnimation) 1f else 0f, tween(1000), label = "alpha")

    LaunchedEffect(Unit) { startAnimation = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC))))
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale).padding(24.dp)
        ) {
            Text(
                text = "Eşleştiniz! 🎉",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (matchedUser.matchScore > 0) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "%${matchedUser.matchScore} Uyumlu",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy((-30).dp)) {
                ProfileAvatar(url = currentUserPhoto, pulse = pulse)
                ProfileAvatar(url = matchedUser.photoUrl, pulse = pulse)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "${matchedUser.displayName} de seni beğendi!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onNavigateToChat,
                enabled = !isCreatingChat,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF2575FC)),
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                shape = CircleShape
            ) {
                if (isCreatingChat) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color(0xFF2575FC))
                } else {
                    Text("İLK MESAJI GÖNDER", fontWeight = FontWeight.Black)
                }
            }

            TextButton(onClick = onNavigateBack) {
                Text("KEŞFETMEYE DEVAM ET", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ProfileAvatar(url: String?, pulse: Float) {
    Surface(
        modifier = Modifier.size(130.dp).scale(pulse).clip(CircleShape),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
        shadowElevation = 8.dp
    ) {
        AsyncImage(
            model = url ?: "https://via.placeholder.com/150",
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
