package com.example.linuxtermuxpanel.ui.commands

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.linuxtermuxpanel.data.model.Command
import com.example.linuxtermuxpanel.ui.theme.LinuxTermuxPanelTheme
import com.example.linuxtermuxpanel.ui.viewmodel.CommandViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(navController: NavHostController) {
    val viewModel: CommandViewModel = hiltViewModel()
    val commands by viewModel.commands.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val executionResult by viewModel.lastExecutionResult.collectAsState()

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var editingCommand by remember { mutableStateOf<Command?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var dialogDescription by remember { mutableStateOf("") }
    var dialogCommand by remember { mutableStateOf("") }
    var dialogEnvironment by remember { mutableStateOf("Termux") }
    var dialogIcon by remember { mutableStateOf("") }
    var dialogIsFavorite by remember { mutableStateOf(false) }
    var dialogRunInBackground by remember { mutableStateOf(false) }
    var dialogNeedsInteractiveTerminal by remember { mutableStateOf(false) }
    var dialogEnvironmentExpanded by remember { mutableStateOf(false) }

    // Confirmation dialog state
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var commandToDelete by remember { mutableStateOf<Command?>(null) }

    // Result dialog state
    var showResultDialog by remember { mutableStateOf(false) }

    // Environment options
    val environments = listOf("Termux", "Ubuntu")

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

    // Launch effect to show result dialog when execution completes
    LaunchedEffect(executionResult) {
        if (executionResult != null && !isExecuting) {
            showResultDialog = true
        }
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
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            label = { Text("اسم الأمر") },
                            value = dialogName,
                            onValueChange = { dialogName = it },
                            isError = dialogName.isEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            label = { Text("الوصف (اختياري)") },
                            value = dialogDescription,
                            onValueChange = { dialogDescription = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            label = { Text("الأمر") },
                            value = dialogCommand,
                            onValueChange = { dialogCommand = it },
                            isError = dialogCommand.isEmpty(),
                            modifier = Modifier.fillMaxWidth()
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
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (dialogEnvironmentExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                environments.forEach { env ->
                                    Text(
                                        text = env,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                dialogEnvironment = env
                                                dialogEnvironmentExpanded = false
                                            }
                                            .padding(12.dp)
                                            .background(
                                                if (dialogEnvironment == env)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    Color.Transparent
                                            )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            label = { Text("الأيقونة (اختياري)") },
                            value = dialogIcon,
                            onValueChange = { dialogIcon = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "المفضلة",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = dialogIsFavorite,
                                onCheckedChange = { dialogIsFavorite = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تشغيل في الخلفية",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = dialogRunInBackground,
                                onCheckedChange = { dialogRunInBackground = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "يحتاج Terminal تفاعلي",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = dialogNeedsInteractiveTerminal,
                                onCheckedChange = { dialogNeedsInteractiveTerminal = it }
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
                    TextButton(onClick = { showDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }

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
                                modifier = Modifier.weight(1f)
                            )
                            if (command.isFavorite) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        if (!command.description.isNullOrEmpty()) {
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
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // زر التشغيل
                            FilledTonalButton(
                                onClick = { viewModel.executeCommand(command) },
                                enabled = !isExecuting,
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = if (isExecuting) Icons.Default.HourglassTop else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تشغيل")
                            }
                            // زر التعديل
                            OutlinedButton(
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
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "تعديل",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تعديل")
                            }
                            // زر الحذف
                            OutlinedButton(
                                onClick = {
                                    showConfirmationDialog = true
                                    commandToDelete = command
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for deletion
    if (showConfirmationDialog && commandToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog = false
                commandToDelete = null
            },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف الأمر \"${commandToDelete?.name}\"؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        commandToDelete?.let { viewModel.deleteCommand(it) }
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

    // Result dialog
    if (showResultDialog && executionResult != null) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                viewModel.clearExecutionResult()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (executionResult!!.success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (executionResult!!.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (executionResult!!.success) "تم التنفيذ بنجاح" else "فشل التنفيذ")
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "رمز الخروج: ${executionResult!!.exitCode}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (executionResult!!.output.isNotEmpty()) {
                        Text(
                            text = "الناتج:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = executionResult!!.output,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (executionResult!!.error.isNotEmpty()) {
                        Text(
                            text = "الخطأ:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = executionResult!!.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResultDialog = false
                        viewModel.clearExecutionResult()
                    }
                ) {
                    Text("حسنًا")
                }
            }
        )
    }
}
