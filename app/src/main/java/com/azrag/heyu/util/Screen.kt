package com.azrag.heyu.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * HeyU Uygulaması Navigasyon Haritası.
 * Tüm rotalar merkezi olarak burada mühürlenmiştir.
 */
sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    // --- Başlangıç ve Açılış Rotaları ---
    object Splash : Screen("splash")
    object Start : Screen("start")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")

    // --- Ana Menü (Dashboard - BottomNav) ---
    object Discover : Screen("discover", "Keşfet", Icons.Default.Explore)
    object EventBoard : Screen("event_board", "Etkinlikler", Icons.Default.Event)
    object MessageList : Screen("message_list", "Mesajlar", Icons.Default.Chat)
    object ProfileView : Screen("profile_view", "Profil", Icons.Default.Person)

    // --- Onboarding (Kayıt Akışı) ---
    object Onboarding1 : Screen("onboarding_name_age")
    object Onboarding2 : Screen("onboarding_major")
    object Onboarding3 : Screen("onboarding_hobbies")
    object Onboarding4 : Screen("onboarding_picture")

    // --- Ayarlar ve Profil Yönetimi ---
    object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
    object EditProfile : Screen("edit_profile", "Profili Düzenle", Icons.Default.Edit)
    object BlockedUsers : Screen("blocked_users", "Engellenenler", Icons.Default.Block)

    // MÜHÜRLENDİ: Başkasının profilini görme rotası
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }

    // --- Parametreli Rotalar (Güvenli ID Geçişi) ---

    // Eşleşme Başarılı Ekranı
    object MatchSuccess : Screen("match_success/{matchedUserId}") {
        fun createRoute(userId: String) = "match_success/$userId"
    }

    // Sohbet Odası
    object Chat : Screen("chat/{chatRoomId}") {
        fun createRoute(chatRoomId: String) = "chat/$chatRoomId"
    }

    // Etkinlik Detayı
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    // Etkinlik Yönetimi
    object AddEvent : Screen("add_event", "Etkinlik Oluştur", Icons.Default.Add)

    // MÜHÜRLENDİ: Admin Paneli rotası (Gelecekteki kontrol için)
    object AdminPanel : Screen("admin_panel", "Yönetim", Icons.Default.AdminPanelSettings)
}
