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
        Image(
            painter = painterResource(id = R.drawable.baslangic), 
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // GÜNCELLENDİ: Tam Agbalumo tarzı, büyük ve baskın logo
            Text(
                text = "heyU!",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = LogoFontFamily,
                    fontSize = 90.sp, // Agbalumo için daha etkileyici bir boyut
                    color = colorScheme.primary,
                    letterSpacing = (-2).sp // Fontun karakteri gereği harfleri biraz yakınlaştırdım
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSignUpClicked,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "Create account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLoginClicked,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = "I have already an account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
