package com.azrag.heyu.ui.signup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen3(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigateToPicture: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // ÇÖZÜM: Değerleri 'by' delegesiyle alarak tip netleştiriliyor
    val bio by viewModel.bio
    val selectedHobbies by viewModel.selectedHobbies // ViewModel'de List<String> olmalı
    val error by viewModel.error
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    val allHobbies = listOf(
        "Müzik", "Spor", "Film", "Kitap", "Gezi", "Oyun",
        "Yemek", "Sanat", "Dans", "Teknoloji", "Doğa", "Moda"
    )

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            if (event is OnboardingViewModel.UiEvent.NavigateToPicture) {
                onNavigateToPicture()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Oluştur (3/4)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("İlgi Alanların", style = MaterialTheme.typography.titleLarge)
            Text("(En fazla 5 tane seçebilirsin)", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                allHobbies.forEach { hobby ->
                    // ÇÖZÜM: 'contains' artık doğru List tipinde çalışacak
                    val isSelected = selectedHobbies.contains(hobby)

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onHobbyClicked(hobby) },
                        label = { Text(hobby) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Done, contentDescription = "Seçildi") }
                        } else {
                            null
                        }
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            Text("Kendinden Bahset", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                // ViewModel.bio bir MutableState<String> ise .value üzerinden güncellenmeli
                onValueChange = { if (it.length <= 150) viewModel.bio.value = it },
                label = { Text("Kısaca kendini anlat...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                supportingText = {
                    Text(
                        text = "${bio.length} / 150",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                }
            )
            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { viewModel.onHobbiesBioNextClicked() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("İLERİ")
                }
            }
        }
    }
}
