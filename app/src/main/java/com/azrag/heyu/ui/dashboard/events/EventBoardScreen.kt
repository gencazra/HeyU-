package com.azrag.heyu.ui.dashboard.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.data.model.Event
import com.azrag.heyu.util.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBoardScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    // ViewModel'daki uiState akışını dinle
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            // Admin kontrolü mühürlendi
            if (uiState.isCurrentUserAdmin) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEvent.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Etkinlik Ekle")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoadingList -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.events.isEmpty() -> {
                    Text(
                        text = "Henüz bir etkinlik paylaşılmamış.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.events) { event ->
                            EventBoardCard(
                                event = event,
                                onClick = {
                                    // Detay sayfasına güvenli yönlendirme
                                    navController.navigate(Screen.EventDetail.createRoute(event.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventBoardCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Görsel yükleme: Boşsa varsayılan profil/etkinlik görseli
            Image(
                painter = rememberAsyncImagePainter(
                    model = event.imageUrl.ifBlank { R.drawable.ic_default_profile }
                ),
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Başlık
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Organizatör
                Text(
                    text = event.organizer,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // TARİH HATASI ÇÖZÜMÜ:
                // Modeldeki eventDate (String) önceliklidir, yoksa serverTimestamp kullanılır.
                val displayDate = remember(event) {
                    if (event.eventDate.isNotBlank()) {
                        "${event.eventDate} ${event.eventTime}"
                    } else {
                        event.serverTimestamp?.let {
                            SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale("tr")).format(it)
                        } ?: "Tarih belirtilmemiş"
                    }
                }

                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
