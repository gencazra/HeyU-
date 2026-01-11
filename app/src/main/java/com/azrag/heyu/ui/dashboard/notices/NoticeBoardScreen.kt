package com.azrag.heyu.ui.dashboard.notices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azrag.heyu.ui.shared.NoticeCard
import com.azrag.heyu.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen(
    navController: NavController,
    viewModel: NoticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddNotice.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Duyuru Ekle")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoadingList) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (uiState.listError != null) {
                Text(uiState.listError!!, Modifier.align(Alignment.Center))
            } else {
                if (uiState.notices.isEmpty()) {
                    Text("Henüz duyuru veya etkinlik yok.", Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.notices, key = { it.id }) { notice ->
                            NoticeCard(
                                notice = notice,
                                onImInClicked = { viewModel.onImInClicked(notice.id) },
                                onClick = { navController.navigate(Screen.NoticeDetail.createRoute(notice.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
