package com.azrag.heyu.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.azrag.heyu.data.YeditepeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen2(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val selectedFaculty by viewModel.selectedFaculty
    val selectedMajor by viewModel.major
    val selectedClass by viewModel.classLevel

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Profilini Tamamla (2/4)", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Eğitim Bilgilerin", 
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Üniversite hayatına dair detayları paylaş.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            var facultyExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = facultyExpanded, onExpandedChange = { facultyExpanded = it }) {
                OutlinedTextField(
                    value = selectedFaculty, 
                    onValueChange = {}, 
                    readOnly = true,
                    label = { Text("Fakülte Seçin") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                ExposedDropdownMenu(
                    expanded = facultyExpanded, 
                    onDismissRequest = { facultyExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    YeditepeData.faculties.forEach { faculty ->
                        DropdownMenuItem(
                            text = { Text(faculty, color = MaterialTheme.colorScheme.onSurface) }, 
                            onClick = { 
                                viewModel.onFacultySelected(faculty)
                                facultyExpanded = false 
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            var majorExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = majorExpanded, onExpandedChange = { if (selectedFaculty.isNotEmpty()) majorExpanded = it }) {
                OutlinedTextField(
                    value = selectedMajor, 
                    onValueChange = {}, 
                    readOnly = true, 
                    enabled = selectedFaculty.isNotEmpty(),
                    label = { Text("Bölüm Seçin") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = majorExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                ExposedDropdownMenu(
                    expanded = majorExpanded, 
                    onDismissRequest = { majorExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    YeditepeData.majorsByFaculty[selectedFaculty]?.forEach { major ->
                        DropdownMenuItem(
                            text = { Text(major, color = MaterialTheme.colorScheme.onSurface) }, 
                            onClick = { 
                                viewModel.major.value = major
                                majorExpanded = false 
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            var classExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = classExpanded, onExpandedChange = { classExpanded = it }) {
                OutlinedTextField(
                    value = selectedClass, 
                    onValueChange = {}, 
                    readOnly = true,
                    label = { Text("Kaçıncı Sınıfsın?") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                ExposedDropdownMenu(
                    expanded = classExpanded, 
                    onDismissRequest = { classExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    YeditepeData.classLevels.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level, color = MaterialTheme.colorScheme.onSurface) }, 
                            onClick = { 
                                viewModel.classLevel.value = level
                                classExpanded = false 
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = { viewModel.onMajorNextClicked() }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                enabled = selectedFaculty.isNotEmpty() && selectedMajor.isNotEmpty() && selectedClass.isNotEmpty()
            ) {
                Text("İLERİ", fontWeight = FontWeight.Bold)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is OnboardingViewModel.UiEvent.NavigateToHobbies) { 
                onNext() 
            }
        }
    }
}
