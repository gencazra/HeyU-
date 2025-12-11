// ----- ImagePickers.kt DOSYASININ İÇERİĞİ -----

package com.azrag.heyu.ui.common

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Profil resmi için kullanılan dairesel resim seçici.
 */
@Composable
fun ImagePickerCircle(
    uri: Uri?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape) // Kenarlıkların düzgün görünmesi için clip
            .background(Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri == null) {
            Text("Seç", color = Color.DarkGray)
        } else {
            AsyncImage(
                model = uri,
                contentDescription = "Profil Resmi",
                modifier = Modifier.fillMaxSize(), // Box'ı tamamen doldurması için
                contentScale = ContentScale.Crop // Resmi kırparak sığdır
            )
        }
    }
}

/**
 * Ekstra fotoğraflar için kullanılan dikdörtgen resim seçici.
 */
@Composable
fun ImagePickerRectangle(
    uri: Uri?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(100.dp) // Boyutunu biraz artırmak daha iyi görünebilir
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri == null) {
            Text("Ekle", color = Color.DarkGray)
        } else {
            AsyncImage(
                model = uri,
                contentDescription = "Ekstra Resim",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

