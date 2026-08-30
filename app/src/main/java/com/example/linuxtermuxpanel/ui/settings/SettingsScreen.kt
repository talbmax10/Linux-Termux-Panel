package com.example.linuxtermuxpanel.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.linuxtermuxpanel.ui.theme.LinuxTermuxPanelTheme
import com.example.linuxtermuxpanel.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinuxTermuxPanelTheme {
                SettingsScreen(navController = rememberNavController())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsState()
    val currentSettings = settings ?: Settings()

    // We'll use temporary state for the fields that are being edited
    var termuxPackageName by remember { mutableStateOf(currentSettings.termuxPackageName) }
    var ubuntuLoginCommand by remember { mutableStateOf(currentSettings.ubuntuLoginCommand) }
    var ubuntuDistributionName by remember { mutableStateOf(currentSettings.ubuntuDistributionName) }
    var autoWrapUbuntuCommands by remember { mutableStateOf(currentSettings.autoWrapUbuntuCommands) }
    var timeoutSeconds by remember { mutableStateOf(currentSettings.timeoutSeconds) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "إعداد Termux",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("اسم حزمة Termux") },
                value = termuxPackageName,
                onValueChange = { termuxPackageName = it },
                isError = termuxPackageName.isEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "أمر الدخول إلى Ubuntu",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("أمر الدخول (مثال: proot-distro login ubuntu)") },
                value = ubuntuLoginCommand,
                onValueChange = { ubuntuLoginCommand = it },
                isError = ubuntuLoginCommand.isEmpty()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("اسم توزيع Ubuntu (افتراضي: ubuntu)") },
                value = ubuntuDistributionName,
                onValueChange = { ubuntuDistributionName = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "إعدادات تنفيذ الأوامر",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لَفّ أوامر Ubuntu تلقائيًا",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = autoWrapUbuntuCommands,
                    onValueChange = { autoWrapUbuntuCommands = it }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("مهلة تنفيذ الأمر (بالثواني)") },
                value = timeoutSeconds.toString(),
                onValueChange = {
                    // Try to parse to int, if fails, keep the old value
                    try {
                        timeoutSeconds = it.toInt()
                    } catch (e: NumberFormatException) {
                        // Keep the old value, show error? We'll just keep the old value and maybe show an error.
                        // For simplicity, we'll just keep the old value and not update the state.
                    }
                },
                isError = timeoutSeconds <= 0
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Save button
            Button(
                onClick = {
                    // Save the settings
                    viewModel.saveSettings(
                        Settings(
                            termuxPackageName = termuxPackageName,
                            ubuntuLoginCommand = ubuntuLoginCommand,
                            ubuntuDistributionName = ubuntuDistributionName,
                            autoWrapUbuntuCommands = autoWrapUbuntuCommands,
                            timeoutSeconds = timeoutSeconds
                        )
                    )
                    // Navigate back to dashboard
                    navController.navigate("dashboard")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ الإعدادات")
            }
        }
    }
}
