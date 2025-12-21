// Dosya Yolu: app/src/main/java/com/azrag/heyu/ui/dashboard/events/AddEventScreen.kt
package com.azrag.heyu.ui.dashboard.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
// MÜHÜRLENDİ: 'by' delegesinin tip çıkarımı yapabilmesi için gerekli importlar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import java.util.*

/**
 * HeyU - Yeni Etkinlik Oluşturma Ekranı.
 * Moderasyon filtresi ve Yeditepe topluluk standartlarına göre mühürlenmiştir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // ViewModel State'leri - 'by' kullanımı artık hata vermeyecektir
    val title by viewModel.title
    val description by viewModel.description
    val organizer by viewModel.organizer
    val location by viewModel.location
    val imageUrl by viewModel.imageUrl
    val dateText by viewModel.dateText
    val timeText by viewModel.timeText
    val isSaving by viewModel.isSaving
    val formError by viewModel.formError

    // Tarih Seçici Diyaloğu
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            viewModel.onDateChange(year, month, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Saat Seçici Diyaloğu
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            viewModel.onTimeChange(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // Başarılı kayıt sonrası tetiklenecek efekt
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.SaveSuccess -> {
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etkinlik Oluştur", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bilgilendirme Kartı
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kampüs topluluğuna uygun, yapıcı bir dil kullanmaya özen gösterin.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Etkinlik Başlığı
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                label = { Text("Etkinlik Başlığı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Örn: Kulüp Tanışma Toplantısı") }
            )

            // Organizatör
            OutlinedTextField(
                value = organizer,
                onValueChange = { viewModel.organizer.value = it },
                label = { Text("Düzenleyen Kulüp / Kişi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Detaylar
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Etkinlik Detayları") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Öğrencilere etkinliğinizden bahsedin...") }
            )

            // Konum
            OutlinedTextField(
                value = location,
                onValueChange = { viewModel.location.value = it },
                label = { Text("Konum / Salon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Örn: Rektörlük Binası Mavi Salon") }
            )

            // Görsel
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { viewModel.imageUrl.value = it },
                label = { Text("Afiş Görsel URL (Opsiyonel)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tarih ve Saat Seçimi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (dateText.isEmpty()) "Tarih Seç" else dateText)
                }
                Button(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (timeText.isEmpty()) "Saat Seç" else timeText)
                }
            }

            // Hata Mesajı Alanı
            formError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Kaydet Butonu
            Button(
                onClick = { viewModel.onSaveClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSaving,
                shape = MaterialTheme.shapes.large
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("ETKİNLİĞİ YAYINLA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
