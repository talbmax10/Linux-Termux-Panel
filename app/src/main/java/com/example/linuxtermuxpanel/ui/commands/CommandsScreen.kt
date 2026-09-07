package com.example.linuxtermuxpanel.ui.commands

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.linuxtermuxpanel.data.model.Command
import com.example.linuxtermuxpanel.data.model.Environments
import com.example.linuxtermuxpanel.ui.components.ExecutionResultDialog
import com.example.linuxtermuxpanel.ui.navigation.navigateBack
import com.example.linuxtermuxpanel.ui.viewmodel.CommandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(navController: NavHostController) {
    val viewModel: CommandViewModel = hiltViewModel()
    val commands by viewModel.commands.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val runningCommandId by viewModel.runningCommandId.collectAsState()
    val executionResult by viewModel.lastExecutionResult.collectAsState()

    var editorState by remember { mutableStateOf<CommandFormState?>(null) }
    var commandToDelete by remember { mutableStateOf<Command?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الأوامر") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editorState = CommandFormState() }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة أمر جديد")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        if (commands.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد أوامر محفوظة. اضغط + لإضافة أمر جديد.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(commands, key = { it.id }) { command ->
                    CommandCard(
                        command = command,
                        isRunning = runningCommandId == command.id,
                        executionDisabled = isExecuting,
                        onRun = { viewModel.executeCommand(command) },
                        onEdit = { editorState = CommandFormState.from(command) },
                        onDelete = { commandToDelete = command },
                        onToggleFavorite = { viewModel.toggleFavorite(command) }
                    )
                }
            }
        }
    }

    editorState?.let { state ->
        CommandEditorDialog(
            state = state,
            onStateChange = { editorState = it },
            onDismiss = { editorState = null },
            onSave = { form ->
                val command = form.toCommand()
                if (form.id == 0L) viewModel.addCommand(command) else viewModel.updateCommand(command)
                editorState = null
            }
        )
    }

    commandToDelete?.let { command ->
        AlertDialog(
            onDismissRequest = { commandToDelete = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف الأمر \"${command.name}\"؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCommand(command)
                        commandToDelete = null
                    }
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { commandToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    executionResult?.let { result ->
        ExecutionResultDialog(result = result, onDismiss = { viewModel.clearExecutionResult() })
    }
}

@Composable
private fun CommandCard(
    command: Command,
    isRunning: Boolean,
    executionDisabled: Boolean,
    onRun: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (command.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "المفضلة",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (!command.description.isNullOrBlank()) {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onRun,
                    enabled = !executionDisabled
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تشغيل")
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommandEditorDialog(
    state: CommandFormState,
    onStateChange: (CommandFormState) -> Unit,
    onDismiss: () -> Unit,
    onSave: (CommandFormState) -> Unit
) {
    val isValid = state.name.isNotBlank() && state.command.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.id == 0L) "إضافة أمر جديد" else "تعديل الأمر") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    label = { Text("اسم الأمر") },
                    value = state.name,
                    onValueChange = { onStateChange(state.copy(name = it)) },
                    isError = state.name.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    label = { Text("الوصف (اختياري)") },
                    value = state.description,
                    onValueChange = { onStateChange(state.copy(description = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    label = { Text("الأمر") },
                    value = state.command,
                    onValueChange = { onStateChange(state.copy(command = it)) },
                    isError = state.command.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "البيئة", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Environments.all.forEach { environment ->
                        FilterChip(
                            selected = state.environment == environment,
                            onClick = { onStateChange(state.copy(environment = environment)) },
                            label = { Text(environment) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SwitchRow(
                    label = "المفضلة",
                    checked = state.isFavorite,
                    onCheckedChange = { onStateChange(state.copy(isFavorite = it)) }
                )
                SwitchRow(
                    label = "تشغيل في الخلفية",
                    checked = state.runInBackground,
                    onCheckedChange = { onStateChange(state.copy(runInBackground = it)) }
                )
                SwitchRow(
                    label = "يحتاج طرفية تفاعلية",
                    checked = state.needsInteractiveTerminal,
                    onCheckedChange = { onStateChange(state.copy(needsInteractiveTerminal = it)) }
                )
                if (state.needsInteractiveTerminal) {
                    Text(
                        text = "في الوضع التفاعلي يُفتح الأمر داخل جلسة Termux ولا يمكن التقاط المخرجات.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(state) }, enabled = isValid) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** حالة نموذج إضافة/تعديل الأمر. */
data class CommandFormState(
    val id: Long = 0L,
    val name: String = "",
    val description: String = "",
    val command: String = "",
    val environment: String = Environments.TERMUX,
    val icon: String = "",
    val isFavorite: Boolean = false,
    val runInBackground: Boolean = false,
    val needsInteractiveTerminal: Boolean = false,
    val createdAt: java.util.Date = java.util.Date()
) {
    fun toCommand(): Command = Command(
        id = id,
        name = name.trim(),
        description = description.trim().ifBlank { null },
        command = command.trim(),
        environment = environment,
        icon = icon.trim().ifBlank { null },
        isFavorite = isFavorite,
        runInBackground = runInBackground,
        needsInteractiveTerminal = needsInteractiveTerminal,
        createdAt = createdAt
    )

    companion object {
        fun from(command: Command): CommandFormState = CommandFormState(
            id = command.id,
            name = command.name,
            description = command.description.orEmpty(),
            command = command.command,
            environment = command.environment,
            icon = command.icon.orEmpty(),
            isFavorite = command.isFavorite,
            runInBackground = command.runInBackground,
            needsInteractiveTerminal = command.needsInteractiveTerminal,
            createdAt = command.createdAt
        )
    }
}
