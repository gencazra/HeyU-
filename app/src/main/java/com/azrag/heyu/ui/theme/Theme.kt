// Konum: app/src/main/java/com/azrag/heyu/ui/theme/Theme.kt

package com.azrag.heyu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Figma prototipine uygun renk paletimiz.
// Uygulamamız hep "karanlık mod" gibi görüneceği için sadece darkColorScheme'i düzenleyeceğiz.
private val DarkColorScheme = darkColorScheme(
    primary = HeyUYellow,           // Ana etkileşim rengi (butonlar, aktif ikonlar)
    onPrimary = HeyUBlue,           // Ana rengin üzerindeki metin/ikon rengi (Sarı buton içindeki yazı)
    secondary = HeyUYellowVariant,  // İkincil vurgu rengi
    background = HeyUBlue,          // Ekranların ana arka planı
    surface = HeyUBlue,             // Kart, dialog gibi yüzeylerin rengi
    onBackground = HeyUText,        // Arka plan üzerindeki metin rengi
    onSurface = HeyUText,           // Yüzeyler üzerindeki metin rengi
    tertiary = HeyUGray,            // Daha az önemli metinler veya ikonlar için
    primaryContainer = HeyUYellow,  // Farklı bir kullanım alanı için
    onPrimaryContainer = HeyUBlue   //
)

@Composable
fun HeyUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Bu uygulamada her zaman karanlık tema kullanacağız
    dynamicColor: Boolean = false, // Android 12+ dinamik renklerini kapalı tutuyoruz
    content: @Composable () -> Unit
) {
    // Prototip her zaman koyu olduğu için lightColorScheme'i de aynı yapıyoruz ki
    // telefon açık modda bile olsa tasarımımız bozulmasın.
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar (en üstteki saat, pil ikonu olan bar) rengini arka planla aynı yapıyoruz.
            window.statusBarColor = colorScheme.background.toArgb()
            // Status bar ikonlarının aydınlık (beyaz) görünmesini sağlıyoruz.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typography.kt dosyasından yazı tiplerini alacak
        content = content
    )
}
