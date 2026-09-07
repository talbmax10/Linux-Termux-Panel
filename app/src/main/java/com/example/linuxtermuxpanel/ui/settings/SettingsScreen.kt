package com.example.linuxtermuxpanel.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.linuxtermuxpanel.ui.navigation.navigateBack
import com.example.linuxtermuxpanel.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val currentMessage = message
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(currentMessage)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "إعداد Termux", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("اسم حزمة Termux") },
                value = settings.termuxPackageName,
                onValueChange = viewModel::onTermuxPackageChanged,
                isError = settings.termuxPackageName.isBlank(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "مثال: com.termux أو com.termux.fdroid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "بيئة Ubuntu", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("أمر الدخول إلى Ubuntu") },
                value = settings.ubuntuLoginCommand,
                onValueChange = viewModel::onUbuntuLoginCommandChanged,
                isError = settings.ubuntuLoginCommand.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "لَفّ أوامر Ubuntu تلقائيًا", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.autoWrapUbuntuCommands,
                    onCheckedChange = viewModel::onAutoWrapChanged
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "تنفيذ الأوامر", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("مهلة تنفيذ الأمر (بالثواني)") },
                value = if (settings.timeoutSeconds == 0) "" else settings.timeoutSeconds.toString(),
                onValueChange = { text ->
                    val digits = text.filter { it.isDigit() }.take(4)
                    viewModel.onTimeoutChanged(digits.toIntOrNull() ?: 0)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = settings.timeoutSeconds <= 0,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.save() },
                enabled = settings.termuxPackageName.isNotBlank() &&
                    settings.ubuntuLoginCommand.isNotBlank() &&
                    settings.timeoutSeconds > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ الإعدادات")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("استعادة الإعدادات الافتراضية")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "لتشغيل الأوامر عبر Termux يجب إضافة السطر allow-external-apps=true " +
                    "إلى الملف ~/.termux/termux.properties ثم تنفيذ الأمر termux-reload-settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
