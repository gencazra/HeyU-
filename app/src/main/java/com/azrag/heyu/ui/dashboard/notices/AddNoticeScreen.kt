// Dosya Yolu: ui/dashboard/notices/AddNoticeScreen.kt
package com.azrag.heyu.ui.dashboard.notices

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoticeScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddNoticeViewModel = hiltViewModel()
) {
    // Bu dosya gönderdiğin haliyle büyük ölçüde doğruydu.
    // Sadece birkaç küçük düzeltme ve viewModel'e uyum sağlama yeterli.
    // Koddaki temel yapı aynı kalıyor.
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Proje", "Etüt", "Kahve", "Spor", "Oyun")
    var selectedCategory by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddNoticeUiState.Loading -> {
                isLoading = true
            }
            is AddNoticeUiState.Success -> {
                isLoading = false
                Toast.makeText(context, "Duyuru başarıyla paylaşıldı!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is AddNoticeUiState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.onUiStateHandled() // Hata gösterildikten sonra state'i sıfırla.
            }
            is AddNoticeUiState.Idle -> {
                isLoading = false
            }
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
        // LazyColumn yerine Column + verticalScroll kullanmak bu form için daha verimli.
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Başlık (Örn: CS:GO girecek var mı?)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama (Örn: Akşam rank kasacak +2 arıyoruz.)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                enabled = !isLoading
            )

            Text("Kategori Seç", style = MaterialTheme.typography.titleMedium)
            // Google Accompanist FlowRow, kategoriler satıra sığmazsa alta geçer.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 4.dp
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = (category == selectedCategory),
                        onClick = { if (!isLoading) selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Butonu en alta itmek için

            Button(
                onClick = {
                    // ViewModel'deki fonksiyonu çağır
                    viewModel.createNotice(title, description, selectedCategory)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("PAYLAŞ")
                }
            }
        }
    }
}
