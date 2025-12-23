package com.azrag.heyu

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.azrag.heyu.ui.dashboard.DashboardScreen
import com.azrag.heyu.ui.dashboard.discover.MatchAnimationScreen
import com.azrag.heyu.ui.dashboard.events.AddEventScreen
import com.azrag.heyu.ui.dashboard.events.EventDetailScreen
import com.azrag.heyu.ui.dashboard.messages.ChatScreen
import com.azrag.heyu.ui.login.*
import com.azrag.heyu.ui.signup.*
import com.azrag.heyu.ui.start.StartScreen
import com.azrag.heyu.ui.theme.HeyUTheme
import com.azrag.heyu.util.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "settings")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { isLoading }

        var startDestination by mutableStateOf(Screen.Login.route)

        lifecycleScope.launch {
            val onboardingCompleted = dataStore.data.map {
                it[booleanPreferencesKey("onboarding_completed")] ?: false
            }.first()

            val currentUser = Firebase.auth.currentUser

            startDestination = when {
                currentUser != null -> {
                    if (!currentUser.isEmailVerified) {
                        Screen.Login.route
                    } else if (!onboardingCompleted) {
                        Screen.Onboarding1.route
                    } else {
                        Screen.Dashboard.route
                    }
                }
                !onboardingCompleted && currentUser == null -> Screen.Start.route
                else -> Screen.Login.route
            }

            isLoading = false
        }

        setContent {
            HeyUTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoading) {
                        val navController = rememberNavController()
                        val scope = rememberCoroutineScope()

                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable(Screen.Start.route) {
                                StartScreen(
                                    onLoginClicked = { navController.navigate(Screen.Login.route) },
                                    onSignUpClicked = { navController.navigate(Screen.Signup.route) }
                                )
                            }

                            composable(Screen.Login.route) {
                                LoginScreen(
                                    onLoginSuccess = { hasProfile ->
                                        val user = Firebase.auth.currentUser
                                        if (user?.isEmailVerified == true) {
                                            val dest = if (hasProfile) Screen.Dashboard.route else Screen.Onboarding1.route
                                            navController.navigate(dest) { popUpTo(0) }
                                        }
                                    },
                                    onNavigateToSignUp = { navController.navigate(Screen.Signup.route) },
                                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                                )
                            }

                            composable(Screen.Signup.route) {
                                SignupScreen(
                                    onSignupSuccess = {
                                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.ForgotPassword.route) {
                                ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
                            }

                            composable(Screen.Onboarding1.route) {
                                OnboardingNameAgeScreen(
                                    onNavigateToMajor = { navController.navigate(Screen.Onboarding2.route) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Onboarding2.route) {
                                OnboardingScreen2(
                                    onNext = { navController.navigate(Screen.Onboarding3.route) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Onboarding3.route) {
                                OnboardingScreen3(
                                    onNavigateToPicture = { navController.navigate(Screen.Onboarding4.route) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Onboarding4.route) {
                                OnboardingScreen4(
                                    onOnboardingComplete = {
                                        scope.launch {
                                            dataStore.edit { it[booleanPreferencesKey("onboarding_completed")] = true }
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Dashboard.route) {
                                DashboardScreen(mainNavController = navController)
                            }

                            composable(Screen.AddEvent.route) {
                                AddEventScreen(navController = navController)
                            }

                            composable(
                                route = Screen.EventDetail.route,
                                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("eventId") ?: ""
                                EventDetailScreen(eventId = id, navController = navController)
                            }

                            composable(
                                route = Screen.Chat.route,
                                arguments = listOf(navArgument("chatRoomId") { type = NavType.StringType })
                            ) { ChatScreen(navController = navController) }

                            composable(
                                route = Screen.MatchSuccess.route,
                                arguments = listOf(navArgument("matchedUserId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val userId = backStackEntry.arguments?.getString("matchedUserId") ?: ""
                                MatchAnimationScreen(navController = navController, matchedUserId = userId)
                            }
                        }
                    }
                }
            }
        }
    }
}
