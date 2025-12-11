// Dosya: ui/dashboard/events/AddEventScreen.kt

package com.azrag.heyu.ui.dashboard.events

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEventViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val categories = listOf("Kulüp", "Spor", "Müzik", "Sanat", "Teknoloji", "Parti")
    var selectedCategory by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Galeri'den resim seçimi için launcher
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    // ViewModel durumunu dinle
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddEventUiState.Loading -> isLoading = true
            is AddEventUiState.Success -> {
                isLoading = false
                Toast.makeText(context, "Etkinlik başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is AddEventUiState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            is AddEventUiState.Idle -> isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Etkinlik Oluştur") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Görsel Yükleme Alanı
            item {
                OutlinedButton(
                    onClick = { if (!isLoading) imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Seçilen Etkinlik Görseli",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(40.dp))
                            Text("Etkinlik Görseli Yükle")
                        }
                    }
                }
            }

            // Diğer Girdi Alanları
            item {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Etkinlik Başlığı") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading)
            }
            item {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Açıklama") }, modifier = Modifier.fillMaxWidth().height(150.dp), enabled = !isLoading)
            }
            item {
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Konum") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading)
            }
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Kategori Seç", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Start)
                    FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 8.dp) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = (category == selectedCategory),
                                onClick = { if (!isLoading) selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                }
            }

            // Paylaş Butonu
            item {
                Button(
                    onClick = {
                        viewModel.createEvent(
                            title = title,
                            description = description,
                            location = location,
                            category = selectedCategory,
                            date = Timestamp.now(),
                            imageUri = imageUri
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("ETKİNLİĞİ PAYLAŞ")
                    }
                }
            }
        }
    }
}
