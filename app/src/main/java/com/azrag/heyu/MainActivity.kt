package com.azrag.heyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel // ÖNEMLİ: Bu import'u ekle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azrag.heyu.ui.auth.ForgotPasswordScreen
import com.azrag.heyu.ui.auth.LoginScreen
import com.azrag.heyu.ui.auth.SignupScreen
import com.azrag.heyu.ui.dashboard.DashboardScreen
import com.azrag.heyu.ui.profile.CreateProfileScreen
import com.azrag.heyu.ui.profile.EditProfileScreen
import com.azrag.heyu.ui.profile.FeedbackScreen
import com.azrag.heyu.ui.profile.MyProfileScreen // ÖNEMLİ: Yeni ekranımızı import et
import com.azrag.heyu.ui.profile.SettingsScreen
import com.azrag.heyu.ui.theme.HeyUTheme // Temanın adının 'HeyUTheme' olduğunu varsayıyorum
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeyUTheme {
                val navController = rememberNavController()
                val startDestination = if (Firebase.auth.currentUser != null) {
                    Screens.Dashboard.route // Giriş yapmışsa Dashboard'a
                } else {
                    Screens.Login.route // Yapmamışsa Login'e
                }
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }
    }
}

// Rotalar için Sealed Class - Yeni "MyProfile" rotası eklendi
sealed class Screens(val route: String) {
    object Login : Screens("login_screen")
    object Signup : Screens("signup_screen")
    object ForgotPassword : Screens("forgot_password_screen")
    object CreateProfile : Screens("create_profile_screen/{fullName}") {
        fun createRoute(fullName: String) = "create_profile_screen/$fullName"
    }
    object Dashboard : Screens("dashboard_screen")

    // --- PROFİL EKRANLARI İÇİN YENİ ROTALAR ---
    object MyProfile : Screens("my_profile_screen") // Ana profil ekranı rotası
    object EditProfile : Screens("edit_profile_screen")
    object Settings : Screens("settings_screen")
    object Feedback : Screens("feedback_screen")
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        // --- GİRİŞ VE KAYIT AKIŞI (Değişiklik yok) ---
        composable(Screens.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screens.Dashboard.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate(Screens.Signup.route) },
                onNavigateToForgotPassword = { navController.navigate(Screens.ForgotPassword.route) }
            )
        }
        composable(Screens.Signup.route) {
            SignupScreen(
                onSignupSuccess = { fullName ->
                    navController.navigate(Screens.CreateProfile.createRoute(fullName)) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                },
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
                onProfileCreated = {
                    navController.navigate(Screens.Dashboard.route) {
                        popUpTo(Screens.CreateProfile.route) { inclusive = true }
                    }
                }
            )
        }

        // --- ANA EKRAN (Dashboard) ---
        composable(Screens.Dashboard.route) {
            DashboardScreen(
                // Not: Dashboard'dan direkt Ayarlar veya Profili Düzenle yerine
                // önce ana profil ekranına gitmek daha mantıklı bir akış olabilir.
                onNavigateToMyProfile = { navController.navigate(Screens.MyProfile.route) }
            )
        }

        // --- PROFİL AKIŞI (TAMAMEN YENİDEN DÜZENLENDİ) ---

        // Ana Profil Ekranı
        composable(Screens.MyProfile.route) {
            MyProfileScreen(
                // ViewModel'i Hilt'in sağlaması için sadece hiltViewModel() çağrılır.
                // Bu ekran kendi ViewModel'ini yönetir.
                viewModel = hiltViewModel(),
                onNavigateToEditProfile = { navController.navigate(Screens.EditProfile.route) },
                onNavigateToSettings = { navController.navigate(Screens.Settings.route) },
                onLogoutSuccess = {
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) // Tüm geçmişi temizle
                    }
                }
            )
        }

        // Profili Düzenleme Ekranı
        composable(Screens.EditProfile.route) {
            EditProfileScreen(
                // Bu ekran da kendi ViewModel'ini Hilt'ten alır.
                // Bu sayede ekranlar arası gereksiz veri taşıma olmaz.
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Ayarlar Ekranı (SettingsScreen'in ViewModel'e ihtiyacı var mı?)
        // Eğer SettingsScreen'in de ProfileViewModel'e ihtiyacı varsa, o da kendi hiltViewModel()'ini çağırabilir.
        // Eğer yoksa, parametre olarak istemesine de gerek yoktur.
        composable(Screens.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                // onNavigateToFeedback gibi diğer navigasyonlar buraya eklenebilir
            )
        }

        composable(Screens.Feedback.route) {
            FeedbackScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
