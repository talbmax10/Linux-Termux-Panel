package com.example.linuxtermuxpanel.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.linuxtermuxpanel.LinuxTermuxPanelApplication
import com.example.linuxtermuxpanel.R
import com.example.linuxtermuxpanel.ui.commands.CommandsScreen
import com.example.linuxtermuxpanel.ui.dashboard.MainScreen
import com.example.linuxtermuxpanel.ui.history.HistoryScreen
import com.example.linuxtermuxpanel.ui.services.ServicesScreen
import com.example.linuxtermuxpanel.ui.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We use setContent to set the Compose content.
        setContent {
            LinuxTermuxPanelTheme {
                // We consume the window insets to avoid system UI overlapping.
                val windowInsets = WindowInsets
                val navController = rememberNavController()
                // We set the start destination to "dashboard"
                NavHost(
                    navController = navController,
                    startDestination = "dashboard"
                ) {
                    composable("dashboard") {
                        MainScreen(navController = navController)
                    }
                    composable("commands") {
                        CommandsScreen(navController = navController)
                    }
                    composable("services") {
                        ServicesScreen(navController = navController)
                    }
                    composable("history") {
                        HistoryScreen(navController = navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController = navController)
                    }
                }
            }
        }
    }
}