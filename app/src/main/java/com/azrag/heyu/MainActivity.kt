// Dosya: MainActivity.kt

package com.azrag.heyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azrag.heyu.ui.dashboard.DashboardScreen // DİKKAT: DashboardScreen'in doğru import edildiğinden emin ol
import com.azrag.heyu.ui.dashboard.events.AddEventScreen
import com.azrag.heyu.ui.dashboard.events.EventDetailScreen
import com.azrag.heyu.ui.dashboard.notices.AddNoticeScreen
import com.azrag.heyu.ui.login.ForgotPasswordScreen
import com.azrag.heyu.ui.login.LoginScreen
import com.azrag.heyu.ui.profile.*
import com.azrag.heyu.ui.signup.SignupScreen
import com.azrag.heyu.ui.theme.HeyUTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeyUTheme {
                val navController = rememberNavController()
                // Kullanıcı giriş yapmışsa Dashboard'a, yapmamışsa Login'e yönlendir
                val startDestination = if (Firebase.auth.currentUser != null) {
                    Screens.Dashboard.route
                } else {
                    Screens.Login.route
                }
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}

// Ekran rotaları - Burası doğru, değişiklik gerekmiyor
sealed class Screens(val route: String) {
    object Login : Screens("login_screen")
    object Signup : Screens("signup_screen")
    object ForgotPassword : Screens("forgot_password_screen")
    object CreateProfile : Screens("create_profile_screen/{fullName}") {
        fun createRoute(fullName: String) = "create_profile_screen/$fullName"
    }
    object Dashboard : Screens("dashboard_screen")
    object MyProfile : Screens("my_profile_screen")
    object EditProfile : Screens("edit_profile_screen")
    object Settings : Screens("settings_screen")
    object Feedback : Screens("feedback_screen")
    object AddNotice : Screens("add_notice_screen")
    object AddEvent : Screens("add_event_screen")
    object EventDetail : Screens("event_detail_screen/{eventId}") {
        fun createRoute(eventId: String) = "event_detail_screen/$eventId"
    }
}

// Navigasyon grafiği - **ÖNEMLİ DEĞİŞİKLİKLER BURADA**
@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        // --- GİRİŞ / KAYIT AKIŞI ---
        composable(Screens.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screens.Dashboard.route) { popUpTo(Screens.Login.route) { inclusive = true } } },
                onNavigateToSignup = { navController.navigate(Screens.Signup.route) },
                onNavigateToForgotPassword = { navController.navigate(Screens.ForgotPassword.route) }
            )
        }
        composable(Screens.Signup.route) {
            SignupScreen(
                onSignupSuccess = { fullName -> navController.navigate(Screens.CreateProfile.createRoute(fullName)) { popUpTo(Screens.Login.route) { inclusive = true } } },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screens.ForgotPassword.route) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screens.CreateProfile.route) { backStackEntry ->
            val fullName = backStackEntry.arguments?.getString("fullName") ?: ""
            CreateProfileScreen(
                fullName = fullName,
                onProfileCreated = { navController.navigate(Screens.Dashboard.route) { popUpTo(Screens.CreateProfile.route) { inclusive = true } } }
            )
        }

        // --- ANA EKRAN (DASHBOARD) VE İÇERİK AKIŞI ---
        composable(Screens.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screens.AddNotice.route) {
            AddNoticeScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screens.AddEvent.route) {
            AddEventScreen(onNavigateBack = { navController.popBackStack() })
        }

        // --- BU BLOK DÜZELTİLDİ ---
        // EventDetailScreen artık eventId'yi parametre olarak almıyor.
        // Kendi ViewModel'i içinden Hilt aracılığıyla alıyor.
        composable(Screens.EventDetail.route) {
            EventDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- PROFİL VE AYARLAR AKIŞI ---
        composable(Screens.MyProfile.route) {
            // hiltViewModel() çağrısını burada yapmak daha temizdir
            MyProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
                onNavigateToEditProfile = { navController.navigate(Screens.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Screens.Settings.route) },
                onLogoutSuccess = {
                    // Güvenli çıkış ve yönlendirme
                    Firebase.auth.signOut()
                    navController.navigate(Screens.Login.route) {
                        // Geri tuşuna basıldığında tekrar uygulamaya girmesini engelle
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
        composable(Screens.EditProfile.route) {
            // Bu ekran da aynı ProfileViewModel'i kullanabilir
            EditProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screens.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFeedback = { navController.navigate(Screens.Feedback.route) },
            )
        }
        composable(Screens.Feedback.route) {
            FeedbackScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
