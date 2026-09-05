package com.example.linuxtermuxpanel.ui.services

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.linuxtermuxpanel.data.model.Service
import com.example.linuxtermuxpanel.ui.theme.LinuxTermuxPanelTheme
import com.example.linuxtermuxpanel.ui.viewmodel.ServiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@AndroidEntryPoint
class ServicesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinuxTermuxPanelTheme {
                ServicesScreen(navController = rememberNavController())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(navController: NavHostController) {
    val viewModel: ServiceViewModel = hiltViewModel()
    val services by viewModel.services.collectAsState()

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<Service?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var dialogStartCommand by remember { mutableStateOf("") }
    var dialogStopCommand by remember { mutableStateOf("") }
    var dialogStatusCommand by remember { mutableStateOf("") }
    var dialogRestartCommand by remember { mutableStateOf("") }
    var dialogEnvironment by remember { mutableStateOf("Termux") }
    var dialogEnvironmentExpanded by remember { mutableStateOf(false) }

    // Confirmation dialog state - MUST be before Scaffold
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<Service?>(null) }

    // Environment options
    val environments = listOf("Termux", "Ubuntu")

    val onSaveService = {
        val serviceToSave = editingService ?: Service(
            name = dialogName,
            startCommand = dialogStartCommand,
            stopCommand = dialogStopCommand,
            statusCommand = dialogStatusCommand,
            restartCommand = dialogRestartCommand,
            environment = dialogEnvironment
        ).copy(
            name = dialogName,
            startCommand = dialogStartCommand,
            stopCommand = dialogStopCommand,
            statusCommand = dialogStatusCommand,
            restartCommand = dialogRestartCommand,
            environment = dialogEnvironment
        )

        if (editingService != null) {
            viewModel.updateService(serviceToSave)
        } else {
            viewModel.addService(serviceToSave)
        }

        showDialog = false
        editingService = null
        dialogName = ""
        dialogStartCommand = ""
        dialogStopCommand = ""
        dialogStatusCommand = ""
        dialogRestartCommand = ""
        dialogEnvironment = "Termux"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الخدمات") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showDialog = true
                    editingService = null
                    dialogName = ""
                    dialogStartCommand = ""
                    dialogStopCommand = ""
                    dialogStatusCommand = ""
                    dialogRestartCommand = ""
                    dialogEnvironment = "Termux"
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة خدمة جديدة")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(if (editingService == null) "إضافة خدمة جديدة" else "تعديل الخدمة")
                },
                text = {
                    Column {
                        TextField(
                            label = { Text("اسم الخدمة") },
                            value = dialogName,
                            onValueChange = { dialogName = it },
                            isError = dialogName.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            label = { Text("أمر التشغيل") },
                            value = dialogStartCommand,
                            onValueChange = { dialogStartCommand = it },
                            isError = dialogStartCommand.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            label = { Text("أمر الإيقاف (اختياري)") },
                            value = dialogStopCommand,
                            onValueChange = { dialogStopCommand = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            label = { Text("أمر فحص الحالة (اختياري)") },
                            value = dialogStatusCommand,
                            onValueChange = { dialogStatusCommand = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            label = { Text("أمر إعادة التشغيل (اختياري)") },
                            value = dialogRestartCommand,
                            onValueChange = { dialogRestartCommand = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            label = { Text("البيئة") },
                            value = dialogEnvironment,
                            onValueChange = { dialogEnvironment = it },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { dialogEnvironmentExpanded = !dialogEnvironmentExpanded }
                                ) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        if (dialogEnvironmentExpanded) {
                            Column {
                                environments.forEach { env ->
                                    Text(
                                        text = env,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                dialogEnvironment = env
                                                dialogEnvironmentExpanded = false
                                            }
                                            .padding(16.dp)
                                            .background(
                                                if (dialogEnvironment == env)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                else
                                                    Color.Transparent
                                            )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (dialogName.isNotEmpty() && dialogStartCommand.isNotEmpty()) {
                                onSaveService()
                            }
                        }
                    ) {
                        Text("حفظ")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(services) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.Start)
                            )
                        }
                        Text(
                            text = "أمر التشغيل: ${service.startCommand}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                        if (!service.stopCommand.isNullOrEmpty()) {
                            Text(
                                text = "أمر الإيقاف: ${service.stopCommand}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }
                        if (!service.statusCommand.isNullOrEmpty()) {
                            Text(
                                text = "أمر فحص الحالة: ${service.statusCommand}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }
                        if (!service.restartCommand.isNullOrEmpty()) {
                            Text(
                                text = "أمر إعادة التشغيل: ${service.restartCommand}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            )
                        }
                        Text(
                            text = "البيئة: ${service.environment}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    showDialog = true
                                    editingService = service
                                    dialogName = service.name
                                    dialogStartCommand = service.startCommand
                                    dialogStopCommand = service.stopCommand ?: ""
                                    dialogStatusCommand = service.statusCommand ?: ""
                                    dialogRestartCommand = service.restartCommand ?: ""
                                    dialogEnvironment = service.environment
                                }
                            ) {
                                Text("تعديل")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    showConfirmationDialog = true
                                    serviceToDelete = service
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("حذف")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmationDialog && serviceToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog = false
                serviceToDelete = null
            },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف الخدمة \"${serviceToDelete?.name}\"؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        serviceToDelete?.let { viewModel.deleteService(it) }
                        showConfirmationDialog = false
                        serviceToDelete = null
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        serviceToDelete = null
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}
