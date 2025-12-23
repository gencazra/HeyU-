package com.azrag.heyu.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {

    object Splash : Screen("splash")
    object Start : Screen("start")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")

    object Discover : Screen("discover", "Keşfet", Icons.Default.Explore)
    object EventBoard : Screen("event_board", "Etkinlikler", Icons.Default.Event)
    object MessageList : Screen("message_list", "Mesajlar", Icons.Default.Chat)
    object ProfileView : Screen("profile_view", "Profil", Icons.Default.Person)


    object Onboarding1 : Screen("onboarding_name_age")
    object Onboarding2 : Screen("onboarding_major")
    object Onboarding3 : Screen("onboarding_hobbies")
    object Onboarding4 : Screen("onboarding_picture")


    object Settings : Screen("settings", "Ayarlar", Icons.Default.Settings)
    object EditProfile : Screen("edit_profile", "Profili Düzenle", Icons.Default.Edit)
    object BlockedUsers : Screen("blocked_users", "Engellenenler", Icons.Default.Block)

    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }


    object MatchSuccess : Screen("match_success/{matchedUserId}") {
        fun createRoute(userId: String) = "match_success/$userId"
    }


    object Chat : Screen("chat/{chatRoomId}") {
        fun createRoute(chatRoomId: String) = "chat/$chatRoomId"
    }


    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    object AddEvent : Screen("add_event", "Etkinlik Oluştur", Icons.Default.Add)


    object AdminPanel : Screen("admin_panel", "Yönetim", Icons.Default.AdminPanelSettings)
}
