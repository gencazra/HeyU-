// Dosya: ui/dashboard/notices/AddNoticeScreen.kt
package com.azrag.heyu.ui.dashboard.notices

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoticeScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddNoticeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Proje", "Etüt", "Kahve", "Spor")
    var selectedCategory by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddNoticeUiState.Loading -> isLoading = true
            is AddNoticeUiState.Success -> {
                isLoading = false
                Toast.makeText(context, "Duyuru başarıyla paylaşıldı!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is AddNoticeUiState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            is AddNoticeUiState.Idle -> isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Duyuru Oluştur") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(0.dp)) }
            item {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Başlık") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading)
            }
            item {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Açıklama") }, modifier = Modifier.fillMaxWidth().height(200.dp), enabled = !isLoading)
            }
            item {
                Text("Kategori Seç", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Start)
                FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 8.dp) {
                    categories.forEach { category ->
                        FilterChip(selected = (category == selectedCategory), onClick = { if (!isLoading) selectedCategory = category }, label = { Text(category) })
                    }
                }
            }
            item {
                Button(
                    onClick = { viewModel.createNotice(title, description, selectedCategory) },
                    modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("PAYLAŞ")
                    }
                }
            }
        }
    }
}
