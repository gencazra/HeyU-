package com.azrag.heyu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.azrag.heyu.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// GÜNCELLENDİ: Logo fontu Agbalumo yapıldı
val LogoFontName = GoogleFont("Agbalumo")
val LogoFontFamily = FontFamily(
    Font(googleFont = LogoFontName, fontProvider = provider)
)

// GÜNCELLENDİ: Yazı fontu Montserrat yapıldı
val MontserratFontName = GoogleFont("Montserrat")
val MontserratFontFamily = FontFamily(
    Font(googleFont = MontserratFontName, fontProvider = provider)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    displayLarge = TextStyle(
        fontFamily = LogoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 80.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
