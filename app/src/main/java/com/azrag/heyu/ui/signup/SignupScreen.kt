package com.azrag.heyu.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azrag.heyu.ui.theme.HeyUTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen( // Fonksiyon adı küçük harfle düzeltildi
    onSignupSuccess: (fullName: String) -> Unit, // Parametre adı düzeltildi
    onNavigateBack: () -> Unit // Geri dönme parametresi eklendi
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    fun validateEmail(email: String): String? {
        return if (email.isNotBlank() && !email.endsWith("@std.yeditepe.edu.tr")) {
            "Lütfen Yeditepe maili giriniz."
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hesap Oluştur") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Ad Soyad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = validateEmail(it)
                },
                label = { Text("E-posta") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            if (generalError != null) {
                Text(
                    text = generalError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    generalError = null
                    emailError = validateEmail(email)

                    if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                        generalError = "Tüm alanlar doldurulmalıdır."
                        return@Button
                    }
                    if (emailError != null) {
                        return@Button
                    }
                    if (password.length < 6) {
                        generalError = "Şifre en az 6 karakter olmalıdır."
                        return@Button
                    }

                    isLoading = true

                    Firebase.auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                // Başarılı! `fullName` bilgisini de göndererek navigasyonu tetikle.
                                onSignupSuccess(fullName)
                            } else {
                                generalError = task.exception?.localizedMessage ?: "Bilinmeyen bir hata oluştu."
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Hesap Oluştur")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() { // Preview fonksiyon adı küçük harfle düzeltildi
    HeyUTheme {
        SignupScreen(onSignupSuccess = {}, onNavigateBack = {})
    }
}
