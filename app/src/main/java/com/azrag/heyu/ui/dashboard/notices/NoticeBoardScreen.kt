// Dosya: ui/dashboard/notices/NoticeBoardScreen.kt
package com.azrag.heyu.ui.dashboard.notices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.azrag.heyu.Screens
import com.azrag.heyu.data.Notice
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen(
    navController: NavHostController,
    viewModel: NoticeBoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchNotices()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screens.AddNotice.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Duyuru Oluştur")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp).fillMaxSize()) {
            // Arama ve filtreleme çipleri buraya eklenebilir, Event'teki gibi.
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is NoticeBoardUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is NoticeBoardUiState.Success -> {
                        if (state.notices.isEmpty()) {
                            Text("Gösterilecek duyuru bulunamadı.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(state.notices) { notice ->
                                    NoticeCard(notice = notice, onImInClicked = { viewModel.onImInClicked(notice.id) })
                                }
                            }
                        }
                    }
                    is NoticeBoardUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun NoticeCard(notice: Notice, onImInClicked: () -> Unit) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isParticipant = notice.participantIds.contains(currentUserId)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(notice.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(notice.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onImInClicked,
                colors = if (isParticipant) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
            ) {
                Text(if (isParticipant) "BEN DE VARIM (${notice.participantIds.size})" else "BEN DE VARIM")
            }
        }
    }
}
