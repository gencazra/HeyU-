package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azrag.heyu.util.Screen
import com.azrag.heyu.ui.shared.UserCardStack

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    mainNavController: NavController,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val departments = listOf("Hepsi", "Mühendislik", "Hukuk", "İktisat", "Tıp", "Eğitim", "Mimarlık")

    Scaffold(
        topBar = {
            // MÜHÜRLENDİ: Fakülte Filtreleme Satırı
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Text(
                    text = "Keşfet",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(departments) { dept ->
                        FilterChip(
                            selected = uiState.selectedDepartment == dept,
                            onClick = { viewModel.onDepartmentSelected(dept) },
                            label = { Text(dept) },
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.userCards.isEmpty() -> EmptyDiscoverView()
                else -> {
                    UserCardStack(
                        users = uiState.userCards,
                        onSwipe = { user, liked -> viewModel.onCardSwiped(user, liked) }
                    )
                }
            }
        }
    }
}

@Composable
fun MatchScoreBadge(score: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        shadowElevation = 4.dp
    ) {
        Text(
            text = "%$score Uyumlu",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyDiscoverView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 64.sp)
        Text("Görünüşe göre herkesle eşleştin!", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}
