// Dosya: ui/dashboard/events/EventBoardScreen.kt

package com.azrag.heyu.ui.dashboard.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.R
import com.azrag.heyu.Screens
import com.azrag.heyu.data.Event
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBoardScreen(
    navController: NavHostController,
    viewModel: EventBoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()

    // Ekran tekrar görünür olduğunda verileri yeniden çekmek için.
    // Bu, yeni eklenen etkinliğin anında görünmesini sağlar.
    LaunchedEffect(Unit) {
        viewModel.refreshEvents()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screens.AddEvent.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Etkinlik Oluştur")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp).fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChanged,
                label = { Text("Etkinliklerde Ara") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filtrele") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            val chips = listOf("Bugün", "Bu Hafta", "Kulüpler", "Spor", "Müzik")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chips) { chip ->
                    FilterChip(
                        selected = chip == selectedChip,
                        onClick = { viewModel.onChipSelected(chip) },
                        label = { Text(chip) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is EventBoardUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is EventBoardUiState.Success -> {
                        if (state.events.isEmpty()) {
                            Text("Arama kriterlerine uygun etkinlik bulunamadı.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 80.dp) // FAB'ın altında kalmasın
                            ) {
                                items(state.events) { event ->
                                    EventCard(event = event, navController = navController)
                                }
                            }
                        }
                    }
                    is EventBoardUiState.Error -> {
                        Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// EventCard fonksiyonu aynı kalabilir, değişiklik gerekmiyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(event: Event, navController: NavHostController) {
    val dateFormat = SimpleDateFormat("dd MMMM, HH:mm", Locale("tr"))
    Card(
        onClick = { navController.navigate(Screens.EventDetail.createRoute(event.id)) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Image(
                painter = if (event.imageUrl.isNotBlank()) {
                    rememberAsyncImagePainter(event.imageUrl)
                } else {
                    painterResource(id = R.drawable.ic_launcher_background)
                },
                contentDescription = event.title,
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${dateFormat.format(event.date.toDate())} - ${event.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    event.description, style = MaterialTheme.typography.bodySmall,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
