package com.azrag.heyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.azrag.heyu.data.repository.ThemeSetting
import com.azrag.heyu.ui.dashboard.DashboardScreen
import com.azrag.heyu.ui.dashboard.discover.MatchAnimationScreen
import com.azrag.heyu.ui.dashboard.events.AddEventScreen
import com.azrag.heyu.ui.dashboard.events.EventDetailScreen
import com.azrag.heyu.ui.dashboard.messages.ChatScreen
import com.azrag.heyu.ui.login.*
import com.azrag.heyu.ui.profile.CreateProfileScreen
import com.azrag.heyu.ui.profile.SettingsScreen
import com.azrag.heyu.ui.profile.SettingsViewModel
import com.azrag.heyu.ui.signup.*
import com.azrag.heyu.ui.start.StartScreen
import com.azrag.heyu.ui.theme.HeyUTheme
import com.azrag.heyu.util.Screen
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@Composable
fun AnimatedSplashScreen(onAnimationEnd: () -> Unit) {
    val logoText = "heyU!"
    var visibleChars by remember { mutableStateOf(0) }
    
    val backgroundColor = Color(0xFFE67E59) 
    val textColor = Color(0xFFFDEBB3)       

    LaunchedEffect(Unit) {
        for (i in 0..logoText.length) {
            visibleChars = i
            delay(180) 
        }
        delay(800) 
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = logoText.take(visibleChars),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 80.sp 
            ),
            color = textColor
        )
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeSetting by settingsViewModel.themeSetting.collectAsState()
            
            val isDarkTheme = when (themeSetting) {
                ThemeSetting.DARK -> true
                ThemeSetting.LIGHT -> false
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            HeyUTheme(darkTheme = isDarkTheme) {
                var showAnimatedSplash by remember { mutableStateOf(true) }
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val currentUser = auth.currentUser
                    startDestination = if (currentUser != null) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Start.route
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showAnimatedSplash) {
                        AnimatedSplashScreen { showAnimatedSplash = false }
                    } else {
                        startDestination?.let { dest ->
                            val navController = rememberNavController()

                            NavHost(
                                navController = navController,
                                startDestination = dest
                            ) {
                                composable(Screen.Start.route) {
                                    StartScreen(
                                        onLoginClicked = { navController.navigate(Screen.Login.route) },
                                        onSignUpClicked = { navController.navigate(Screen.Signup.route) }
                                    )
                                }

                                composable(Screen.Settings.route) {
                                    SettingsScreen(
                                        onNavigateBack = { navController.popBackStack() },
                                        onLogout = {
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        onEditProfileClick = {
                                            navController.navigate(Screen.EditProfile.route)
                                        }
                                    )
                                }

                                composable(Screen.EditProfile.route) {
                                    CreateProfileScreen(
                                        editMode = true,
                                        onProfileSaved = { navController.popBackStack() },
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }

                                composable(Screen.Login.route) {
                                    LoginScreen(
                                        onLoginSuccess = { hasProfile ->
                                            val newDest = if (hasProfile) Screen.Dashboard.route else Screen.Onboarding1.route
                                            navController.navigate(newDest) { popUpTo(0) }
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
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(0) { inclusive = true }
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
}
