//----- CreateProfileScreen.kt (TÜM GÜNCELLEMELER DAHİL SON HALİ) -----

package com.azrag.heyu.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.azrag.heyu.ui.common.InterestChip
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(
    fullName: String,
    onProfileCreated: () -> Unit,
    viewModel: CreateProfileViewModel = viewModel()
) {
    val photoUri by viewModel.photoUri.collectAsState()
    val faculty by viewModel.faculty.collectAsState()
    val major by viewModel.major.collectAsState()
    val majorsForFaculty by viewModel.majorsForSelectedFaculty.collectAsState()
    val classLevel by viewModel.classLevel.collectAsState()
    val bio by viewModel.bio.collectAsState()
    val selectedInterests by viewModel.selectedInterests.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onPhotoSelected(uri)
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            onProfileCreated()
        }
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Profil Fotoğrafı
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { launcher.launch("image/*") }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = photoUri ?: "https://via.placeholder.com/120"
                        ),
                        contentDescription = "Profil Fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- OKUL BİLGİLERİ ---
                ExposedDropdownMenu(
                    label = "Fakülte",
                    items = viewModel.allFaculties,
                    selectedValue = faculty,
                    onItemSelected = viewModel::onFacultySelected
                )

                if (majorsForFaculty.isNotEmpty()) {
                    ExposedDropdownMenu(
                        label = "Bölüm",
                        items = majorsForFaculty,
                        selectedValue = major,
                        onItemSelected = viewModel::onMajorSelected
                    )
                }

                ExposedDropdownMenu(
                    label = "Sınıf",
                    items = viewModel.allClassLevels,
                    selectedValue = classLevel,
                    onItemSelected = viewModel::onClassLevelSelected
                )
                // -------------------------

                // Hakkımda
                OutlinedTextField(
                    value = bio,
                    onValueChange = viewModel::onBioChange,
                    label = { Text("Hakkımda (Bio)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                // İlgi Alanları
                Text("İlgi Alanların", style = MaterialTheme.typography.titleMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp,
                    mainAxisAlignment = MainAxisAlignment.Center
                ) {
                    viewModel.allInterests.forEach { interest ->
                        InterestChip(
                            interest = interest,
                            isSelected = selectedInterests.contains(interest),
                            onChipClick = viewModel::onInterestChipClick
                        )
                    }
                }

                // Kaydet Butonu
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = saveState !is SaveState.Saving
                ) {
                    if (saveState is SaveState.Saving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                    } else {
                        Text("Profili Oluştur")
                    }
                }

                if (saveState is SaveState.Error) {
                    Text(
                        text = (saveState as SaveState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenu(
    label: String,
    items: List<String>,
    selectedValue: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
