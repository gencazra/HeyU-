package com.azrag.heyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.azrag.heyu.data.repository.ThemeSetting
import com.azrag.heyu.ui.dashboard.DashboardScreen
import com.azrag.heyu.ui.dashboard.messages.ChatScreen
import com.azrag.heyu.ui.login.*
import com.azrag.heyu.ui.profile.SettingsScreen
import com.azrag.heyu.ui.signup.*
import com.azrag.heyu.ui.start.StartScreen
import com.azrag.heyu.ui.theme.HeyUTheme
import com.azrag.heyu.util.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeSetting by viewModel.themeSetting.collectAsState()
            
            val isDarkTheme = when (themeSetting) {
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
                ThemeSetting.DARK -> true
                ThemeSetting.LIGHT -> false
            }

            HeyUTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val currentUser = Firebase.auth.currentUser
                    
                    val startDest = if (currentUser != null) Screen.Dashboard.route else Screen.Start.route

                    NavHost(navController = navController, startDestination = startDest) {
                        composable(Screen.Start.route) {
                            StartScreen(
                                onLoginClicked = { navController.navigate(Screen.Login.route) },
                                onSignUpClicked = { navController.navigate(Screen.Signup.route) }
                            )
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = { navController.navigate(Screen.Dashboard.route) { popUpTo(0) } },
                                onNavigateToSignUp = { navController.navigate(Screen.Signup.route) },
                                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                            )
                        }
                        composable(Screen.Signup.route) {
                            SignupScreen(
                                onSignupSuccess = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(mainNavController = navController)
                        }

                        composable(
                            route = Screen.Chat.route,
                            arguments = listOf(navArgument("chatRoomId") { type = NavType.StringType })
                        ) { 
                            ChatScreen(navController = navController) 
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                                onEditProfileClick = { navController.navigate(Screen.Onboarding1.route) }
                            )
                        }
                    }
                }
            }
        }
    }
}
