// Dosya: ui/dashboard/events/EventDetailScreen.kt

package com.azrag.heyu.ui.dashboard.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.Event
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Ekran her göründüğünde veriyi yenile
    LaunchedEffect(Unit) {
        viewModel.refreshEventDetails()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etkinlik Detayı") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") } }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is EventDetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is EventDetailUiState.Success -> EventDetailsContent(event = state.event, viewModel = viewModel)
                is EventDetailUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EventDetailsContent(event: Event, viewModel: EventDetailViewModel) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr"))
    val currentUserId = Firebase.auth.currentUser?.uid
    val isUserParticipant = event.participantIds.contains(currentUserId)

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Banner Görseli
        item {
            Image(
                painter = if (event.imageUrl.isNotBlank()) {
                    rememberAsyncImagePainter(event.imageUrl)
                } else {
                    painterResource(id = R.drawable.ic_launcher_background) // Placeholder
                },
                contentDescription = "Etkinlik Banner",
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Ana Bilgiler
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = event.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                InfoRow(icon = Icons.Default.CalendarMonth, text = dateFormat.format(event.date.toDate()))
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(icon = Icons.Default.LocationOn, text = event.location)
            }
        }

        // Butonlar
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { viewModel.onJoinLeaveClick() },
                    modifier = Modifier.weight(1f),
                    colors = if (isUserParticipant) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    else ButtonDefaults.buttonColors()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isUserParticipant) "KATILDIN" else "KATIL")
                }
                OutlinedButton(onClick = { /* TODO: Sohbet */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SOHBETİ AÇ")
                }
            }
        }

        // Açıklama
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Açıklama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = event.description, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Katılımcılar
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Katılımcılar (${event.participantIds.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(12.dp))
                if (event.participantIds.isEmpty()) {
                    Text("Henüz katılan kimse yok.", modifier = Modifier.padding(horizontal = 16.dp))
                } else {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(event.participantIds) { participantId -> ParticipantAvatar(userId = participantId) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ParticipantAvatar(userId: String) {
    val userRepository: UserRepository = hiltViewModel<ProfileViewModel>().userRepository

    // Kullanıcının profil resim URL'sini ve adını tutacak state'ler
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf<String?>(null) }

    // Bu Composable ekrana ilk kez geldiğinde veya `userId` değiştiğinde çalışacak olan blok.
    LaunchedEffect(key1 = userId) {
        // Coroutine içinde veritabanından kullanıcı profilini çekiyoruz.
        val userProfile = userRepository.getUserProfile(userId)
        // Gelen kullanıcının bilgilerini state'lere atıyoruz.
        profileImageUrl = userProfile?.profileImageUrl
        fullName = userProfile?.fullName
    }


    val placeholder = painterResource(id = R.drawable.ic_launcher_background)

    // Coil kütüphanesi ile resmi asenkron olarak yükle
    Image(
        painter = if (!profileImageUrl.isNullOrBlank()) {
            rememberAsyncImagePainter(
                model = profileImageUrl,
                error = placeholder, // Hata olursa placeholder göster
                placeholder = placeholder // Yüklenirken placeholder göster
            )
        } else {
            placeholder // URL hiç yoksa placeholder göster
        },
        contentDescription = "Katılımcı: ${fullName ?: "Bilinmiyor"}", // Erişilebilirlik için isim ekleyelim
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
