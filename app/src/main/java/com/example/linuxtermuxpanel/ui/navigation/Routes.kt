package com.example.linuxtermuxpanel.ui.navigation

import androidx.navigation.NavHostController

/** مسارات التنقّل داخل التطبيق. */
object Routes {
    const val DASHBOARD = "dashboard"
    const val COMMANDS = "commands"
    const val SERVICES = "services"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

/**
 * الرجوع للخلف بشكل صحيح.
 * كان الكود السابق يستدعي navigate("dashboard") مما يكدّس نسخًا جديدة من الشاشة
 * في مكدّس التنقّل بدل الرجوع.
 */
fun NavHostController.navigateBack() {
    if (!popBackStack()) {
        navigate(Routes.DASHBOARD) {
            launchSingleTop = true
            popUpTo(Routes.DASHBOARD) { inclusive = true }
        }
    }
}

/** الانتقال إلى وجهة مع تجنّب تكرارها في المكدّس. */
fun NavHostController.navigateTo(route: String) {
    navigate(route) { launchSingleTop = true }
}
