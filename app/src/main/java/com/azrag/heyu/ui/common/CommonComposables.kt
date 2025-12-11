//----- CommonComposables.kt (SON HALİ) -----

package com.azrag.heyu.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Bu fonksiyon zaten vardı
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestChip(
    interest: String,
    isSelected: Boolean,
    onChipClick: (String) -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = { onChipClick(interest) },
        label = { Text(interest) },
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

// YENİ EKLENEN VE PUBLIC HALE GETİRİLEN FONKSİYON
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenu( // 'private' kelimesini sildik
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
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
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
