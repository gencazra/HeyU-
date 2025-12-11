package com.azrag.heyu.ui.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit, // Parametre adı düzeltildi
    onNavigateToForgotPassword: () -> Unit // Eksik parametre eklendi
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("heyU_prefs", Context.MODE_PRIVATE) }

    var email by remember { mutableStateOf(sharedPreferences.getString("email", "") ?: "") }
    var password by remember { mutableStateOf(sharedPreferences.getString("password", "") ?: "") }
    var rememberMe by remember { mutableStateOf(sharedPreferences.getBoolean("rememberMe", false)) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = Firebase.auth

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tekrar Hoş Geldin!", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Giriş yaparak devam et",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-posta Adresi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text("Beni Hatırla", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "E-posta ve şifre boş olamaz.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val editor = sharedPreferences.edit()
                                if (rememberMe) {
                                    editor.putString("email", email)
                                    editor.putString("password", password)
                                    editor.putBoolean("rememberMe", true)
                                } else {
                                    editor.clear()
                                }
                                editor.apply()

                                Toast.makeText(context, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                            isLoading = false
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("GİRİŞ YAP")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Kayıt Ol ekranına yönlendirme metni
            ClickableText(
                text = AnnotatedString("Hesabın yok mu? Kayıt Ol"),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                onClick = { onNavigateToSignup() } // onClick güncellendi
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Şifremi unuttum ekranına yönlendirme metni
            ClickableText(
                text = AnnotatedString("Şifreni mi unuttun?"),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                ),
                onClick = { onNavigateToForgotPassword() } // onClick eklendi
            )
        }
    }
}
