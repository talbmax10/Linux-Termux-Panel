package com.example.linuxtermuxpanel.ui.commands

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.linuxtermuxpanel.data.model.Command
import com.example.linuxtermuxpanel.ui.theme.LinuxTermuxPanelTheme
import com.example.linuxtermuxpanel.ui.viewmodel.CommandViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// We remove the CommandsActivity and keep only the composable.
// The activity will be handled by MainActivity.

@Composable
fun CommandsScreen(navController: NavHostController) {
    val viewModel: CommandViewModel = hiltViewModel()
    val commands by viewModel.commands.collectAsState()

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var editingCommand by remember { mutableStateOf<Command?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }
    var dialogCommand by remember { mutableStateOf("") }
    var dialogEnvironment by remember { mutableStateOf("Termux") } // Default to Termux
    var dialogIcon by remember { mutableStateOf("") }
    var dialogIsFavorite by remember { mutableStateOf(false) }
    var dialogRunInBackground by remember { mutableStateOf(false) }
    var dialogNeedsInteractiveTerminal by remember { mutableStateOf(false) }
    var dialogEnvironmentExpanded by remember { mutableStateOf(false) }

    // Environment options
    val environments = listOf("Termux", "Ubuntu")

    // Handle saving the command
    val onSaveCommand = {
        val commandToSave = editingCommand ?: Command(
            name = dialogName,
            description = dialogDescription,
            command = dialogCommand,
            environment = dialogEnvironment,
            icon = dialogIcon,
            isFavorite = dialogIsFavorite,
            runInBackground = dialogRunInBackground,
            needsInteractiveTerminal = dialogNeedsInteractiveTerminal
        ).copy(
            name = dialogName,
            description = dialogDescription,
            command = dialogCommand,
            environment = dialogEnvironment,
            icon = dialogIcon,
            isFavorite = dialogIsFavorite,
            runInBackground = dialogRunInBackground,
            needsInteractiveTerminal = dialogNeedsInteractiveTerminal
        )

        if (editingCommand != null) {
            viewModel.updateCommand(commandToSave)
        } else {
            viewModel.addCommand(commandToSave)
        }

        // Reset dialog state
        showDialog = false
        editingCommand = null
        dialogName = ""
        dialogDescription = ""
        dialogCommand = ""
        dialogEnvironment = "Termux"
        dialogIcon = ""
        dialogIsFavorite = false
        dialogRunInBackground = false
        dialogNeedsInteractiveTerminal = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الأوامر") },
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
                    editingCommand = null
                    dialogName = ""
                    dialogDescription = ""
                    dialogCommand = ""
                    dialogEnvironment = "Termux"
                    dialogIcon = ""
                    dialogIsFavorite = false
                    dialogRunInBackground = false
                    dialogNeedsInteractiveTerminal = false
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة أمر جديد")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(if (editingCommand == null) "إضافة أمر جديد" else "تعديل الأمر")
                },
                text = {
                    Column {
                        TextField(
                            label = { Text("اسم الأمر") },
                            value = dialogName,
                            onValueChange = { dialogName = it },
                            isError = dialogName.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            label = { Text("الوصف (اختياري)") },
                            value = dialogDescription,
                            onValueChange = { dialogDescription = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            label = { Text("الأمر") },
                            value = dialogCommand,
                            onValueChange = { dialogCommand = it },
                            isError = dialogCommand.isEmpty()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            label = { Text("البيئة") },
                            value = dialogEnvironment,
                            onValueChange = { dialogEnvironment = it },
                            readOnly = true,
                            suffixIcon = {
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
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            label = { Text("الأيقونة (اختياري)") },
                            value = dialogIcon,
                            onValueChange = { dialogIcon = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المفضلة",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = dialogIsFavorite,
                                onValueChange = { dialogIsFavorite = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تشغيل في الخلفية",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = dialogRunInBackground,
                                onValueChange = { dialogRunInBackground = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "يحتاج Terminal تفاعلي",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = dialogNeedsInteractiveTerminal,
                                onValueChange = { dialogNeedsInteractiveTerminal = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (dialogName.isNotEmpty() && dialogCommand.isNotEmpty()) {
                                onSaveCommand()
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

        // List of commands
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(commands) { command ->
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
                                text = command.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.Start)
                            )
                            if (command.isFavorite) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        if (command.description != null && command.description.isNotEmpty()) {
                            Text(
                                text = command.description,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            text = "الأمر: ${command.command}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                        Text(
                            text = "البيئة: ${command.environment}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    showDialog = true
                                    editingCommand = command
                                    dialogName = command.name
                                    dialogDescription = command.description ?: ""
                                    dialogCommand = command.command
                                    dialogEnvironment = command.environment
                                    dialogIcon = command.icon ?: ""
                                    dialogIsFavorite = command.isFavorite
                                    dialogRunInBackground = command.runInBackground
                                    dialogNeedsInteractiveTerminal = command.needsInteractiveTerminal
                                }
                            ) {
                                Text("تعديل")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    // Show confirmation dialog
                                    showConfirmationDialog = true
                                    commandToDelete = command
                                },
                                color = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text("حذف")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for deletion
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var commandToDelete by remember { mutableStateOf<Command?>(null) }

    if (showConfirmationDialog && commandToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog = false
                commandToDelete = null
            },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف الأمر \"${commandToDelete.name}\"؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCommand(commandToDelete!!)
                        showConfirmationDialog = false
                        commandToDelete = null
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        commandToDelete = null
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}