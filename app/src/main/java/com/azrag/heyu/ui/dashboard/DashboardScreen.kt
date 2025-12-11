// ----- DashboardScreen.kt (TAM, DÜZELTİLMİŞ VE ÇALIŞAN HALİ) -----

package com.azrag.heyu.ui.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
// --- ÇÖZÜM 2: EKSİK OLAN IMPORT'U EKLEDİK ---
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.azrag.heyu.ui.dashboard.discover.DiscoverViewModel
import com.azrag.heyu.ui.dashboard.discover.UserProfile
import com.azrag.heyu.ui.theme.HeyUTheme

private data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit // MainActivity'den bu fonksiyonu alıyoruz
) {
    // --- ÇÖZÜM 1: EKSİK DEĞİŞKENLERİ BURAYA GERİ EKLEDİK ---
    val navItems = listOf(
        NavItem("discover", "Keşfet", Icons.Default.Explore),
        NavItem("events", "Etkinlikler", Icons.Default.Celebration),
        NavItem("announcements", "Duyurular", Icons.Default.Campaign),
        NavItem("messages", "Mesajlar", Icons.Default.ChatBubble)
    )
    var selectedRoute by remember { mutableStateOf("discover") }
    // ----------------------------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(navItems.find { it.route == selectedRoute }?.title ?: "HeyU")
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profilim"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedRoute == item.route,
                        onClick = { selectedRoute = item.route },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedRoute) {
                "discover" -> DiscoverScreen()
                "events" -> EventsScreen()
                "announcements" -> AnnouncementsScreen()
                "messages" -> MessagesScreen()
            }
        }
    }
}

// Preview fonksiyonunu da onNavigateToProfile parametresini alacak şekilde güncelleyelim
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    HeyUTheme {
        // Preview'da bu fonksiyonun içi boş olabilir, çünkü tıklama test edilmez.
        DashboardScreen(onNavigateToProfile = {})
    }
}

// Diğer tüm fonksiyonlar (DiscoverScreen, UserProfileCard, EventsScreen vb.)
// AYNEN KALABİLİR, onlarda bir hata yok.
// ... (geri kalan kodun tamamı)

@Composable
private fun DiscoverScreen(viewModel: DiscoverViewModel = viewModel()) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (users.isEmpty()) {
            Text("Görünüşe göre etrafta kimse yok.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(users) { user ->
                    UserProfileCard(user = user)
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = "${user.fullName} profil fotoğrafı",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = user.fullName, style = MaterialTheme.typography.titleLarge)
                Text(text = user.department, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EventsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Etkinlikler Ekranı")
    }
}

@Composable
private fun AnnouncementsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Duyuru Panosu Ekranı")
    }
}

@Composable
private fun MessagesScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mesajlar Ekranı")
    }
}
