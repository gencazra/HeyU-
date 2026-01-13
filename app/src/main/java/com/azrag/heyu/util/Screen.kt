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

    object Discover : Screen("discover", "Discover", Icons.Default.Explore)
    object NoticeBoard : Screen("notice_board", "Notices", Icons.Default.Campaign)
    object Messages : Screen("messages", "Messages", Icons.Default.Chat) // Tek ve net rota
    object ProfileView : Screen("profile_view", "Profile", Icons.Default.Person)

    object Onboarding1 : Screen("onboarding_name_age")
    object Onboarding2 : Screen("onboarding_major")
    object Onboarding3 : Screen("onboarding_hobbies")
    object Onboarding4 : Screen("onboarding_picture")

    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object EditProfile : Screen("edit_profile", "Edit Profile", Icons.Default.Edit)
    object AddNotice : Screen("add_notice", "Add Notice", Icons.Default.Add)

    object Chat : Screen("chat/{chatRoomId}") {
        fun createRoute(chatRoomId: String) = "chat/$chatRoomId"
    }

    object MatchSuccess : Screen("match_success/{matchedUserId}") {
        fun createRoute(userId: String) = "match_success/$userId"
    }
    
    object NoticeDetail : Screen("notice_detail/{noticeId}") {
        fun createRoute(noticeId: String) = "notice_detail/$noticeId"
    }
}
