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
import com.azrag.heyu.ui.dashboard.discover.DiscoverScreen
import com.azrag.heyu.ui.profile.MyProfileScreen
import com.azrag.heyu.util.Screen


@Composable
fun DashboardScreen(mainNavController: NavController) {
    val dashboardNavController = rememberNavController()

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
                                popUpTo(dashboardNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
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
        NavHost(
            navController = dashboardNavController,
            startDestination = Screen.Discover.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Discover.route) {
                DiscoverScreen(mainNavController = mainNavController)
            }

            composable(Screen.EventBoard.route) {
                EventBoardScreen(navController = mainNavController)
            }

            composable(Screen.MessageList.route) {
                ChatListScreen(navController = mainNavController)
            }

            composable(Screen.ProfileView.route) {
                MyProfileScreen(
                    onNavigateToEditProfile = {
                        mainNavController.navigate(Screen.Onboarding1.route)
                    },
                    onNavigateToSettings = {
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
