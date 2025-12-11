// Dosya: app/src/main/java/com/azrag/heyu/ui/profile/SettingItem.kt

package com.azrag.heyu.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Ayarlar ekranında her bir satırı temsil eden yeniden kullanılabilir Composable.
 *
 * @param icon Satırın sol tarafında gösterilecek ikon.
 * @param text Satırda gösterilecek metin.
 * @param isLogout Bu satırın bir "Çıkış Yap" butonu olup olmadığını belirtir. Eğer true ise metin kırmızı olur.
 * @param onClick Satıra tıklandığında tetiklenecek eylem.
 */
@Composable
fun SettingItem(
    icon: ImageVector,
    text: String,
    isLogout: Boolean = false, // Varsayılan olarak false
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Satırın tamamını tıklanabilir yapar
            .padding(horizontal = 24.dp, vertical = 16.dp), // Kenar boşlukları
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // İkon
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            // Çıkış yap butonuysa ikonu da kırmızı yap
            tint = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(20.dp)) // İkon ve metin arası boşluk

        // Metin
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            // Çıkış yap butonuysa metni kırmızı yap
            color = if (isLogout) MaterialTheme.colorScheme.error else Color.Unspecified
        )
    }
}
