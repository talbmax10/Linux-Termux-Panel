package com.example.linuxtermuxpanel.ui.history

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
import com.example.linuxtermuxpanel.data.model.ExecutionHistory
import com.example.linuxtermuxpanel.ui.theme.LinuxTermuxPanelTheme
import com.example.linuxtermuxpanel.ui.viewmodel.ExecutionHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinuxTermuxPanelTheme {
                HistoryScreen(navController = rememberNavController())
            }
        }
    }
}

@Composable
fun HistoryScreen(navController: NavHostController) {
    val viewModel: ExecutionHistoryViewModel = hiltViewModel()
    val history by viewModel.executionHistory.collectAsState()

    // Dialog state for confirmation
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var historyToDelete by remember { mutableStateOf<ExecutionHistory?>(null) }

    // Date formatter for displaying timestamps
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("السجل") },
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
                    // Show confirmation dialog for clearing all history
                    showConfirmationDialog = true
                    historyToDelete = null // Indicates we want to clear all
                }
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "مسح السجل")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        if (history.isEmpty()) {
            // Show a message when there's no history
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "لا توجد سجلات تنفيذ بعد",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            // List of history items
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(history) { item ->
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
                                    text = item.commandText,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .weight(1f)
                                        .wrapContentWidth(Alignment.Start)
                                )
                                // Success icon
                                if (item.success) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.success
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            if (item.output != null && item.output.isNotEmpty()) {
                                Text(
                                    text = "الناتج:",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                )
                                Text(
                                    text = item.output,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                )
                            }
                            if (item.error != null && item.error.isNotEmpty()) {
                                Text(
                                    text = "الخطأ:",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                )
                                Text(
                                    text = item.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalArrangement = Arrangement.SpaceBetween
                                    .verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "رمز الخروج: ${item.exitCode}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "البدء: ${dateFormatter.format(item.startedAt)}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    item.finishedAt?.let { finishedAt ->
                                        Text(
                                            text = "النهاية: ${dateFormatter.format(finishedAt)}",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        showConfirmationDialog = true
                                        historyToDelete = item
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

        // Confirmation dialog for deletion (single item or all)
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmationDialog = false
                    historyToDelete = null
                },
                title = { Text("تأكيد الحذف") },
                text = {
                    if (historyToDelete == null) {
                        Text("هل أنت متأكد من مسح السجل بالكامل؟")
                    } else {
                        Text("هل أنت متأكد من حذف هذا السجل؟")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (historyToDelete == null) {
                                // Clear all history
                                viewModel.deleteAllExecutionHistory()
                            } else {
                                // Delete the specific item
                                viewModel.deleteExecutionHistory(historyToDelete!!)
                            }
                            showConfirmationDialog = false
                            historyToDelete = null
                        }
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showConfirmationDialog = false
                            historyToDelete = null
                        }
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}