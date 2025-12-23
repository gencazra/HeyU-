package com.azrag.heyu.ui.signup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingScreen4(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val imageUri by viewModel.imageUri
    val isLoading by viewModel.isLoading

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.imageUri.value = it } }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            if (event is OnboardingViewModel.UiEvent.OnboardingComplete) { onOnboardingComplete() }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profil Fotoğrafı", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(200.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.AddAPhoto, null, Modifier.size(64.dp), MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(64.dp))
        Button(onClick = { viewModel.onCompleteClicked() }, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = !isLoading) {
            if (isLoading) CircularProgressIndicator() else Text("TAMAMLA VE BİTİR")
        }
    }
}
