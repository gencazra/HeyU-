package com.azrag.heyu.ui.dashboard.notices

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.Notice
import com.azrag.heyu.data.model.UserProfile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeDetailScreen(
    noticeId: String,
    navController: NavController,
    viewModel: NoticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = noticeId) {
        viewModel.loadNoticeDetails(noticeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.selectedNotice?.title ?: "Duyuru Detayı") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoadingDetail) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.detailError != null) {
                Text(text = uiState.detailError!!, modifier = Modifier.align(Alignment.Center))
            } else if (uiState.selectedNotice != null) {
                NoticeDetailsContent(
                    notice = uiState.selectedNotice!!,
                    participants = uiState.participants,
                    onJoinLeaveClick = { viewModel.onImInClicked(noticeId) }
                )
            }
        }
    }
}

@Composable
private fun NoticeDetailsContent(
    notice: Notice,
    participants: List<UserProfile>,
    onJoinLeaveClick: () -> Unit
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isUserParticipant = notice.attendees.contains(currentUserId)

    val displayDate = remember(notice) {
        if (!notice.eventDate.isNullOrBlank()) {
            "${notice.eventDate}, ${notice.eventTime ?: ""}"
        } else {
            notice.timestamp?.let {
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(it)
            } ?: "Tarih belirtilmemiş"
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (!notice.imageUrl.isNullOrBlank()) {
            item {
                Image(
                    painter = rememberAsyncImagePainter(model = notice.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = notice.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "Paylaşan: ${notice.creatorName}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(Icons.Default.CalendarMonth, displayDate)
                notice.location?.let { InfoRow(Icons.Default.LocationOn, it) }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Açıklama", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = notice.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onJoinLeaveClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUserParticipant) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isUserParticipant) "VAZGEÇ" else "KATILIYORUM")
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Katılımcılar (${participants.size})", style = MaterialTheme.typography.titleLarge)
                if (participants.isEmpty()) {
                    Text("Henüz katılan yok.", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(participants) { user ->
                            ParticipantAvatar(user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantAvatar(userProfile: UserProfile) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = rememberAsyncImagePainter(model = userProfile.photoUrl.ifBlank { R.drawable.ic_default_profile }),
            contentDescription = null,
            modifier = Modifier.size(64.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = userProfile.displayName.split(" ").firstOrNull() ?: "",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
