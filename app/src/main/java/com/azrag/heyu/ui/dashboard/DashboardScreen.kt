// Dosya: ui/dashboard/discover/DashboardScreen.kt

package com.azrag.heyu.ui.dashboard.discover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.azrag.heyu.ui.dashboard.events.EventBoardScreen
import com.azrag.heyu.ui.dashboard.notices.NoticeBoardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    // Gelecekte eklenecek navigasyonlar için şimdilik boş
) {
    // Hangi sekmenin seçili olduğunu tutan state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Keşfet", "Etkinlikler", "Duyurular")

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Üstteki Sekme Çubuğu
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            // Seçili sekmeye göre ilgili ekranı göster
            when (selectedTabIndex) {
                0 -> DiscoverScreen()
                1 -> EventBoardScreen(navController = navController) // navController'ı ilet
                2 -> NoticeBoardScreen(navController = navController) // navController'ı ilet
            }
        }
    }
}
