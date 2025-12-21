package com.azrag.heyu.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.azrag.heyu.ui.dashboard.messages.ChatListScreen
import com.azrag.heyu.ui.dashboard.events.EventBoardScreen
import com.azrag.heyu.ui.dashboard.discover.DiscoverScreen // PAKET YOLU GÜNCELLENDİ
import com.azrag.heyu.ui.profile.MyProfileScreen
import com.azrag.heyu.util.Screen

/**
 * DashboardScreen, alt navigasyon (Bottom Bar) ve bu menülerin
 * iç sayfalarını yöneten ana konteynırdır.
 */
@Composable
fun DashboardScreen(mainNavController: NavController) {
    val dashboardNavController = rememberNavController()

    // Bottom Navigation'da gösterilecek ana sayfalar
    val bottomNavItems = listOf(
        Screen.Discover,
        Screen.EventBoard,
        Screen.MessageList,
        Screen.ProfileView
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            dashboardNavController.navigate(screen.route) {
                                // Geri tuşuna basıldığında ana hedefe (Discover) dönmesi için
                                popUpTo(dashboardNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            // Icon null-safety kontrolü
                            screen.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = screen.title
                                )
                            }
                        },
                        label = { screen.title?.let { Text(it) } }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Dashboard içindeki iç navigasyon (Bottom Nav içerikleri)
        NavHost(
            navController = dashboardNavController,
            startDestination = Screen.Discover.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // KEŞFET (Discover) - Mühürlü
            composable(Screen.Discover.route) {
                DiscoverScreen(mainNavController = mainNavController)
            }

            // ETKİNLİKLER (Event Board) - Mühürlü
            composable(Screen.EventBoard.route) {
                EventBoardScreen(navController = mainNavController)
            }

            // MESAJ LİSTESİ (Chat List) - Mühürlü
            composable(Screen.MessageList.route) {
                ChatListScreen(navController = mainNavController)
            }

            // PROFİLİM (My Profile) - Mühürlü
            composable(Screen.ProfileView.route) {
                MyProfileScreen(
                    onNavigateToEditProfile = {
                        // Profil düzenleme (Onboarding akışına geri döner)
                        mainNavController.navigate(Screen.Onboarding1.route)
                    },
                    onNavigateToSettings = {
                        // Ayarlar ekranı mühürlü rotası
                        mainNavController.navigate(Screen.Settings.route)
                    },
                    onLogoutSuccess = {
                        mainNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
