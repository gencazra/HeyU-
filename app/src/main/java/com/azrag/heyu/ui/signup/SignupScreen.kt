// ----- SignUpScreen.kt (YENİ VE GÜNCELLENMİŞ HALİ) -----

package com.azrag.heyu.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azrag.heyu.ui.theme.HeyUTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit // Bu fonksiyon, kayıt başarılı olunca çağrılır
) {
    // --- State Değişkenleri ---
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Yükleme durumu için

    // Hata yönetimi için state'ler
    var emailError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    // --- Doğrulama Fonksiyonu ---
    fun validateEmail(email: String): String? {
        return if (email.isNotBlank() && !email.endsWith("@std.yeditepe.edu.tr")) {
            "Lütfen Yeditepe maili giriniz."
        } else {
            null
        }
    }

    // --- Arayüz ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hesap Oluştur",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- Ad Soyad Alanı (YENİ) ---
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Ad Soyad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- E-posta Alanı (GÜNCELLENDİ) ---
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                // Kullanıcı yazdıkça hatayı kontrol et
                emailError = validateEmail(it)
            },
            label = { Text("E-posta") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError != null, // Hata varsa çerçeveyi kırmızı yap
            supportingText = { // Hata mesajını burada göster
                if (emailError != null) {
                    Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Şifre Alanı ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Genel Hata Mesajı ---
        if (generalError != null) {
            Text(
                text = generalError!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- Hesap Oluştur Butonu (GÜNCELLENDİ) ---
        Button(
            onClick = {
                // Hataları sıfırla
                generalError = null
                emailError = validateEmail(email)

                // Formda hata var mı kontrol et
                if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                    generalError = "Tüm alanlar doldurulmalıdır."
                    return@Button
                }
                if (emailError != null) {
                    return@Button
                }

                // Yüklemeyi başlat
                isLoading = true

                // Firebase ile hesap oluştur
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false // Yüklemeyi bitir
                        if (task.isSuccessful) {
                            // Başarılı! Bir sonraki ekrana git.
                            // TODO: Ad Soyad bilgisini Firestore'a kaydetme adımı burada yapılacak.
                            // Şimdilik sadece navigasyonu tetikliyoruz.
                            onSignUpSuccess()
                        } else {
                            // Hata oluştu
                            generalError = task.exception?.localizedMessage ?: "Bilinmeyen bir hata oluştu."
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Yükleme sırasında butonu pasif yap
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Hesap Oluştur")
            }
        }
    }
}

// Önizleme fonksiyonunu da güncelleyelim
@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    HeyUTheme {
        SignUpScreen(onSignUpSuccess = {})
    }
}
