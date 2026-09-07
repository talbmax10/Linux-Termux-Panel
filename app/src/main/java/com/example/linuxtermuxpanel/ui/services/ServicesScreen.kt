package com.example.linuxtermuxpanel.ui.services

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.linuxtermuxpanel.data.model.Environments
import com.example.linuxtermuxpanel.data.model.Service
import com.example.linuxtermuxpanel.ui.components.ExecutionResultDialog
import com.example.linuxtermuxpanel.ui.navigation.navigateBack
import com.example.linuxtermuxpanel.ui.viewmodel.ServiceAction
import com.example.linuxtermuxpanel.ui.viewmodel.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(navController: NavHostController) {
    val viewModel: ServiceViewModel = hiltViewModel()
    val services by viewModel.services.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val executionResult by viewModel.lastExecutionResult.collectAsState()

    var editorState by remember { mutableStateOf<ServiceFormState?>(null) }
    var serviceToDelete by remember { mutableStateOf<Service?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الخدمات") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editorState = ServiceFormState() }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة خدمة جديدة")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isExecuting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (services.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد خدمات محفوظة. اضغط + لإضافة خدمة جديدة.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(services, key = { it.id }) { service ->
                        ServiceCard(
                            service = service,
                            executionDisabled = isExecuting,
                            onAction = { action -> viewModel.runAction(service, action) },
                            onEdit = { editorState = ServiceFormState.from(service) },
                            onDelete = { serviceToDelete = service }
                        )
                    }
                }
            }
        }
    }

    editorState?.let { state ->
        ServiceEditorDialog(
            state = state,
            onStateChange = { editorState = it },
            onDismiss = { editorState = null },
            onSave = { form ->
                val service = form.toService()
                if (form.id == 0L) viewModel.addService(service) else viewModel.updateService(service)
                editorState = null
            }
        )
    }

    serviceToDelete?.let { service ->
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف الخدمة \"${service.name}\"؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteService(service)
                        serviceToDelete = null
                    }
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { serviceToDelete = null }) { Text("إلغاء") }
            }
        )
    }

    executionResult?.let { result ->
        ExecutionResultDialog(result = result, onDismiss = { viewModel.clearExecutionResult() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCard(
    service: Service,
    executionDisabled: Boolean,
    onAction: (ServiceAction) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(service.environment) }
                )
            }

            CommandLine(label = "التشغيل", value = service.startCommand)
            CommandLine(label = "الإيقاف", value = service.stopCommand)
            CommandLine(label = "الحالة", value = service.statusCommand)
            CommandLine(label = "إعادة التشغيل", value = service.restartCommand)

            Spacer(modifier = Modifier.height(8.dp))

            // أزرار تنفيذ إجراءات الخدمة (كانت مفقودة تمامًا في النسخة السابقة)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServiceAction.values().forEach { action ->
                    val command = when (action) {
                        ServiceAction.START -> service.startCommand
                        ServiceAction.STOP -> service.stopCommand
                        ServiceAction.STATUS -> service.statusCommand
                        ServiceAction.RESTART -> service.restartCommand
                    }
                    if (!command.isNullOrBlank()) {
                        FilterChip(
                            selected = false,
                            enabled = !executionDisabled,
                            onClick = { onAction(action) },
                            label = { Text(action.labelAr) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

@Composable
private fun CommandLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceEditorDialog(
    state: ServiceFormState,
    onStateChange: (ServiceFormState) -> Unit,
    onDismiss: () -> Unit,
    onSave: (ServiceFormState) -> Unit
) {
    val isValid = state.name.isNotBlank() && state.startCommand.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.id == 0L) "إضافة خدمة جديدة" else "تعديل الخدمة") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    label = { Text("اسم الخدمة") },
                    value = state.name,
                    onValueChange = { onStateChange(state.copy(name = it)) },
                    isError = state.name.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    label = { Text("أمر التشغيل") },
                    value = state.startCommand,
                    onValueChange = { onStateChange(state.copy(startCommand = it)) },
                    isError = state.startCommand.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    label = { Text("أمر الإيقاف (اختياري)") },
                    value = state.stopCommand,
                    onValueChange = { onStateChange(state.copy(stopCommand = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    label = { Text("أمر فحص الحالة (اختياري)") },
                    value = state.statusCommand,
                    onValueChange = { onStateChange(state.copy(statusCommand = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    label = { Text("أمر إعادة التشغيل (اختياري)") },
                    value = state.restartCommand,
                    onValueChange = { onStateChange(state.copy(restartCommand = it)) },
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

/** حالة نموذج إضافة/تعديل الخدمة. */
data class ServiceFormState(
    val id: Long = 0L,
    val name: String = "",
    val startCommand: String = "",
    val stopCommand: String = "",
    val statusCommand: String = "",
    val restartCommand: String = "",
    val environment: String = Environments.TERMUX,
    val createdAt: java.util.Date = java.util.Date()
) {
    fun toService(): Service = Service(
        id = id,
        name = name.trim(),
        startCommand = startCommand.trim(),
        stopCommand = stopCommand.trim().ifBlank { null },
        statusCommand = statusCommand.trim().ifBlank { null },
        restartCommand = restartCommand.trim().ifBlank { null },
        environment = environment,
        createdAt = createdAt
    )

    companion object {
        fun from(service: Service): ServiceFormState = ServiceFormState(
            id = service.id,
            name = service.name,
            startCommand = service.startCommand,
            stopCommand = service.stopCommand.orEmpty(),
            statusCommand = service.statusCommand.orEmpty(),
            restartCommand = service.restartCommand.orEmpty(),
            environment = service.environment,
            createdAt = service.createdAt
        )
    }
}
