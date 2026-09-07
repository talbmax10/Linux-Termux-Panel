package com.example.linuxtermuxpanel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.linuxtermuxpanel.ui.commands.CommandsScreen
import com.example.linuxtermuxpanel.ui.dashboard.DashboardScreen
import com.example.linuxtermuxpanel.ui.history.HistoryScreen
import com.example.linuxtermuxpanel.ui.navigation.Routes
import com.example.linuxtermuxpanel.ui.services.ServicesScreen
import com.example.linuxtermuxpanel.ui.settings.SettingsScreen
import com.example.linuxtermuxpanel.ui.theme.LinuxTermuxPanelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinuxTermuxPanelTheme {
                // واجهة التطبيق بالعربية، لذلك نفرض اتجاه من اليمين إلى اليسار
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Routes.DASHBOARD
                        ) {
                            composable(Routes.DASHBOARD) { DashboardScreen(navController) }
                            composable(Routes.COMMANDS) { CommandsScreen(navController) }
                            composable(Routes.SERVICES) { ServicesScreen(navController) }
                            composable(Routes.HISTORY) { HistoryScreen(navController) }
                            composable(Routes.SETTINGS) { SettingsScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}
