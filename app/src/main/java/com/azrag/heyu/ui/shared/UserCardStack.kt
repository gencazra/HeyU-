package com.azrag.heyu.ui.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.UserProfile
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

// Kaydırma yönlerini yöneten dahili enum
private enum class SwipeDirection { LEFT, RIGHT, NONE }

/**
 * Discover sayfasındaki kart yığınını yönetir.
 * [users] gösterilecek kullanıcı listesi.
 * [onSwipe] kullanıcı kaydırıldığında (sağa veya sola) tetiklenen callback.
 */
@Composable
fun UserCardStack(
    users: List<UserProfile>,
    onSwipe: (user: UserProfile, liked: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Görünen kullanıcı listesini yönet (Üstteki kart gittikçe bir sonrakine geçer)
    var visibleUsers by remember(users) { mutableStateOf(users) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val swipeThreshold = screenWidth * 0.4f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (visibleUsers.isEmpty()) {
            Text(
                text = "Çevrendeki herkesle eşleştin! ✨",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Performans için sadece en üstteki 4 kartı render et
        visibleUsers.take(4).reversed().forEachIndexed { index, user ->
            // reversed() sayesinde listenin başındaki eleman en üstte görünür
            val currentQueueIndex = (visibleUsers.take(4).size - 1) - index
            val isTopCard = currentQueueIndex == 0

            var offsetX by remember(user.id) { mutableStateOf(0f) }
            var offsetY by remember(user.id) { mutableStateOf(0f) }
            var swipeDir by remember(user.id) { mutableStateOf(SwipeDirection.NONE) }

            val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "offsetX")
            val animatedOffsetY by animateFloatAsState(targetValue = offsetY, label = "offsetY")

            val rotationZ by animateFloatAsState(
                targetValue = (animatedOffsetX / screenWidth.value) * 15f,
                label = "rotation"
            )

            // Derinlik efekti: Alttaki kartlar biraz daha küçük ve aşağıda durur
            val cardScale by animateFloatAsState(
                targetValue = if (isTopCard) 1f else (1f - (currentQueueIndex * 0.05f)).coerceAtLeast(0.8f),
                label = "cardScale"
            )
            val cardOffsetY by animateDpAsState(
                targetValue = if (isTopCard) 0.dp else (currentQueueIndex * 12).dp,
                label = "cardOffsetY"
            )

            fun triggerSwipeAction(liked: Boolean) {
                coroutineScope.launch {
                    val endX = screenWidth.value * 1.5f * if (liked) 1f else -1f
                    offsetX = endX
                    onSwipe(user, liked)
                    // Animasyon tamamlanmış gibi davranıp kartı listeden çıkar
                    visibleUsers = visibleUsers.filterNot { it.id == user.id }
                }
            }

            Box(
                modifier = Modifier
                    .offset(x = animatedOffsetX.dp, y = animatedOffsetY.dp)
                    .graphicsLayer(
                        rotationZ = rotationZ,
                        scaleX = cardScale,
                        scaleY = cardScale,
                        translationY = with(density) { cardOffsetY.toPx() }
                    )
                    .then(
                        if (isTopCard) {
                            Modifier.pointerInput(user.id) {
                                detectDragGestures(
                                    onDragEnd = {
                                        when {
                                            offsetX > swipeThreshold.value -> triggerSwipeAction(true)
                                            offsetX < -swipeThreshold.value -> triggerSwipeAction(false)
                                            else -> {
                                                offsetX = 0f
                                                offsetY = 0f
                                                swipeDir = SwipeDirection.NONE
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y
                                        swipeDir = if (offsetX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                UserProfileCard(
                    user = user,
                    likeStatusAlpha = if (swipeDir == SwipeDirection.RIGHT) (abs(offsetX) / swipeThreshold.value).coerceIn(0f, 1f) else 0f,
                    dislikeStatusAlpha = if (swipeDir == SwipeDirection.LEFT) (abs(offsetX) / swipeThreshold.value).coerceIn(0f, 1f) else 0f
                )
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    user: UserProfile,
    likeStatusAlpha: Float,
    dislikeStatusAlpha: Float,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            // Profil Fotoğrafı
            Image(
                painter = rememberAsyncImagePainter(
                    model = user.photoUrl.ifEmpty { R.drawable.ic_default_profile }
                ),
                contentDescription = user.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Okunabilirlik için alt gradyan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 400f
                        )
                    )
            )

            // Kaydırma geri bildirimi (Like/Dislike ikonları)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (likeStatusAlpha > 0f) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = Color(0xFF4CAF50).copy(alpha = likeStatusAlpha),
                        modifier = Modifier.size(100.dp).graphicsLayer(scaleX = 0.8f + likeStatusAlpha, scaleY = 0.8f + likeStatusAlpha)
                    )
                }
                if (dislikeStatusAlpha > 0f) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dislike",
                        tint = Color(0xFFF44336).copy(alpha = dislikeStatusAlpha),
                        modifier = Modifier.size(100.dp).graphicsLayer(scaleX = 0.8f + dislikeStatusAlpha, scaleY = 0.8f + dislikeStatusAlpha)
                    )
                }
            }

            // Kullanıcı Bilgileri Katmanı
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Yaş Hesaplama
                val displayAge = user.age ?: user.birthYear?.let { Calendar.getInstance().get(Calendar.YEAR) - it }
                val titleText = if (displayAge != null) "${user.displayName}, $displayAge" else user.displayName

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titleText,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (user.isOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }
                }

                if (user.department.isNotBlank()) {
                    Text(
                        text = user.department,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Hobiler (Chip tarzı görünüm)
                if (user.hobbies.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        user.hobbies.take(3).forEach { hobby ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = hobby,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
