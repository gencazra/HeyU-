package com.azrag.heyu.ui.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Eğitim Bilgilerin", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        var facultyExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = facultyExpanded, onExpandedChange = { facultyExpanded = it }) {
            OutlinedTextField(
                value = selectedFaculty, onValueChange = {}, readOnly = true,
                label = { Text("Fakülte Seçin") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = facultyExpanded, onDismissRequest = { facultyExpanded = false }) {
                YeditepeData.faculties.forEach { faculty ->
                    DropdownMenuItem(text = { Text(faculty) }, onClick = { viewModel.onFacultySelected(faculty); facultyExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        var majorExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = majorExpanded, onExpandedChange = { if (selectedFaculty.isNotEmpty()) majorExpanded = it }) {
            OutlinedTextField(
                value = selectedMajor, onValueChange = {}, readOnly = true, enabled = selectedFaculty.isNotEmpty(),
                label = { Text("Bölüm Seçin") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = majorExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = majorExpanded, onDismissRequest = { majorExpanded = false }) {
                YeditepeData.majorsByFaculty[selectedFaculty]?.forEach { major ->
                    DropdownMenuItem(text = { Text(major) }, onClick = { viewModel.major.value = major; majorExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        var classExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = classExpanded, onExpandedChange = { classExpanded = it }) {
            OutlinedTextField(
                value = selectedClass, onValueChange = {}, readOnly = true,
                label = { Text("Kaçıncı Sınıfsın?") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                YeditepeData.classLevels.forEach { level ->
                    DropdownMenuItem(text = { Text(level) }, onClick = { viewModel.classLevel.value = level; classExpanded = false })
                }
            }
        }

        Spacer(Modifier.height(48.dp))
        Button(onClick = { viewModel.onMajorNextClicked() }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("İLERİ")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is OnboardingViewModel.UiEvent.NavigateToHobbies) { onNext() }
        }
    }
}
