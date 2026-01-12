package com.azrag.heyu.ui.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrag.heyu.R
import com.azrag.heyu.ui.theme.LogoFontFamily

@Composable
fun StartScreen(
    onLoginClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        // Arka Plan Resmi
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // BURAYI R.drawable.login_background YAPMAYI UNUTMA
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Karartma Katmanı (Yazıların okunması için)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.3f)
        ) {}

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo (Temadaki ana renk)
            Text(
                text = "heyU!",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = LogoFontFamily,
                    fontSize = 72.sp,
                    color = colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Hesap Oluştur Butonu (Temadaki Primary rengi)
            Button(
                onClick = onSignUpClicked,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Hesap oluştur",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Zaten hesabım var Butonu (Temadaki Secondary rengi ile Outlined)
            OutlinedButton(
                onClick = onLoginClicked,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, colorScheme.secondary),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorScheme.secondary
                )
            ) {
                Text(
                    text = "Zaten hesabım var",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
