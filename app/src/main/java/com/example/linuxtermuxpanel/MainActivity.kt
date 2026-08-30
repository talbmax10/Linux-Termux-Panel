package com.example.linuxtermuxpanel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.linuxtermuxpanel.ui.commands.CommandsScreen
import com.example.linuxtermuxpanel.ui.dashboard.DashboardScreen
import com.example.linuxtermuxpanel.ui.history.HistoryScreen
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
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") { DashboardScreen(navController) }
                        composable("commands") { CommandsScreen(navController) }
                        composable("services") { ServicesScreen(navController) }
                        composable("history") { HistoryScreen(navController) }
                        composable("settings") { SettingsScreen(navController) }
                    }
                }
            }
        }
    }
}
