package com.azrag.heyu.ui.dashboard.notices

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoticeScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddNoticeViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Genel") }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val eventDate by viewModel.eventDate
    val eventTime by viewModel.eventTime
    val location by viewModel.location

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> viewModel.onDateChange(year, month, day) },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, min -> viewModel.onTimeChange(hour, min) },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    LaunchedEffect(uiState) {
        if (uiState is AddNoticeUiState.Success) {
            onNavigateBack()
            viewModel.onUiStateHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duyuru / Etkinlik Oluştur", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Başlık") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Kategori (Örn: Spor, Akademik)") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Etkinlik Detayları (Opsiyonel)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = location,
                onValueChange = { viewModel.location.value = it },
                label = { Text("Konum") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null) }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DateRange, null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (eventDate.isEmpty()) "Tarih" else eventDate)
                }
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (eventTime.isEmpty()) "Saat" else eventTime)
                }
            }

            if (uiState is AddNoticeUiState.Error) {
                Text(
                    text = (uiState as AddNoticeUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.createNotice(title, description, category) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState !is AddNoticeUiState.Loading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is AddNoticeUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("YAYINLA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
