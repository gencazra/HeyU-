package com.azrag.heyu.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azrag.heyu.R
import com.azrag.heyu.data.model.Feedback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onAdminPanelClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            try {
                val doc = Firebase.firestore.collection("users").document(uid).get().await()
                isAdmin = doc.getBoolean("isAdmin") ?: false
            } catch (e: Exception) {
                isAdmin = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (isAdmin) {
                item {
                    ListItem(
                        headlineContent = { Text("Yönetici Paneli", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Gelen geri bildirimleri incele") },
                        leadingContent = { Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.tertiary) },
                        modifier = Modifier.clickable { onAdminPanelClick() }
                    )
                    HorizontalDivider()
                }
            }

            item {
                ListItem(
                    headlineContent = { Text("Geri Bildirim & Öneriler") },
                    leadingContent = { Icon(Icons.Default.Feedback, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { showFeedbackDialog = true }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Gizlilik Politikası (KVKK)") },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                    modifier = Modifier.clickable { showPrivacyDialog = true }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Kullanım Koşulları") },
                    leadingContent = { Icon(Icons.Default.Gavel, null) },
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Kullanım koşulları yakında eklenecek.", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                ListItem(
                    headlineContent = { Text("Çıkış Yap", color = Color.Red, fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(Icons.Default.Logout, null, tint = Color.Red) },
                    modifier = Modifier.clickable {
                        Firebase.auth.signOut()
                        onLogout()
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Hesabı Sil", color = Color.Gray) },
                    leadingContent = { Icon(Icons.Default.DeleteForever, null, tint = Color.Gray) },
                    modifier = Modifier.clickable { showDeleteAccountDialog = true }
                )
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Gizlilik Politikası") },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    Text(
                        text = stringResource(id = R.string.kvkk_text), // strings.xml'den çeker
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Anladım") }
            }
        )
    }

    if (showFeedbackDialog) {
        var feedbackText by remember { mutableStateOf("") }
        var isSending by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSending) showFeedbackDialog = false },
            title = { Text("Geri Bildirim Gönder") },
            text = {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Mesajınız...") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending,
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSending = true
                            try {
                                val user = Firebase.auth.currentUser
                                val feedback = mapOf(
                                    "userId" to (user?.uid ?: "anonymous"),
                                    "userEmail" to (user?.email ?: "N/A"),
                                    "text" to feedbackText,
                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                                Firebase.firestore.collection("feedback").add(feedback).await()
                                Toast.makeText(context, "Geri bildiriminiz iletildi!", Toast.LENGTH_SHORT).show()
                                showFeedbackDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSending = false
                            }
                        }
                    },
                    enabled = !isSending && feedbackText.isNotBlank()
                ) {
                    if (isSending) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    else Text("Gönder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }, enabled = !isSending) { Text("İptal") }
            }
        )
    }

    if (showDeleteAccountDialog) {
        var isDeleting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteAccountDialog = false },
            title = { Text("Hesabı Kalıcı Olarak Sil?") },
            text = { Text("Bu işlem geri alınamaz. Tüm mesajlarınız, eşleşmeleriniz ve profil verileriniz silinecektir.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            try {
                                val user = Firebase.auth.currentUser
                                val uid = user?.uid
                                if (uid != null) {

                                    Firebase.firestore.collection("users").document(uid).delete().await()
                                    user.delete().await()
                                    Toast.makeText(context, "Hoşçakalın! Hesabınız silindi.", Toast.LENGTH_SHORT).show()
                                    onLogout()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Güvenlik gereği önce tekrar giriş yapmalısınız.", Toast.LENGTH_LONG).show()
                            } finally {
                                isDeleting = false
                                showDeleteAccountDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Her Şeyi Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }, enabled = !isDeleting) { Text("İptal") }
            }
        )
    }
}
