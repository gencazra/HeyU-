// Dosya Yolu: ui/dashboard/discover/DraggableCard.kt
package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.UserProfile
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class SwipeDirection {
    LEFT, RIGHT, NONE
}

@Composable
fun DraggableCard(
    userProfile: UserProfile,
    onSwipe: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var offset by remember { mutableStateOf(Offset.Zero) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val animatedOffset by animateOffsetAsState(
        targetValue = offset,
        animationSpec = tween(durationMillis = 300),
        label = "offsetAnimation"
    )

    val animatedRotationZ by animateFloatAsState(
        targetValue = (animatedOffset.x / 60).coerceIn(-40f, 40f),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .offset(x = animatedOffset.x.dp, y = animatedOffset.y.dp)
            .graphicsLayer(
                rotationZ = animatedRotationZ,
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Belirli bir eşiği geçerse kartı at
                        if (abs(offset.x) > (screenWidth.value / 4)) {
                            val swipeDirection =
                                if (offset.x > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                            onSwipe(swipeDirection)
                        } else {
                            // Kartı yavaşça eski pozisyonuna döndür
                            coroutineScope.launch {
                                // <<< HATA BURADA DÜZELTİLDİ >>>
                                offset = Offset.Zero
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Sürükleme sırasında offset'i direkt güncelle
                        offset += dragAmount
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = userProfile.photoUrl.ifEmpty { R.drawable.ic_default_profile }
                ),
                contentDescription = "Profil Fotoğrafı",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Bilgilerin okunabilir olması için alta gölge efekti
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 600f
                        )
                    )
            )

            // Kullanıcı bilgileri
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = userProfile.displayName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (userProfile.department.isNotBlank()) {
                    Text(
                        text = userProfile.department,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
