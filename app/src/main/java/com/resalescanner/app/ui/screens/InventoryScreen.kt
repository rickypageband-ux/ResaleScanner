package com.resalescanner.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.resalescanner.app.data.export.ExportFormat
import com.resalescanner.app.domain.model.InventoryItem
import com.resalescanner.app.ui.AppViewModel
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: AppViewModel, onAdd: () -> Unit, onEdit: (InventoryItem) -> Unit) {
    val inventory by viewModel.inventory.collectAsState()
    val context = LocalContext.current
    var pendingFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var showExportDialog by remember { mutableStateOf(false) }
    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.openOutputStream(uri)?.use { viewModel.export(pendingFormat, it) } }
            .onSuccess { Toast.makeText(context, "Inventory exported", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show() }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Inventory") }, actions = { IconButton(onClick = { showExportDialog = true }, enabled = inventory.isNotEmpty()) { Icon(Icons.Outlined.FileDownload, "Export") } }) },
        floatingActionButton = { FloatingActionButton(onAdd) { Icon(Icons.Outlined.Add, "Add item") } },
    ) { padding ->
        if (inventory.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) { Text("No inventory yet", style = MaterialTheme.typography.headlineSmall); Text("Scan a barcode or add your first item.") }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(inventory, key = { it.id }) { item -> InventoryRow(item, { onEdit(item) }, { viewModel.delete(item) }) }
            }
        }
    }
    if (showExportDialog) AlertDialog(
        onDismissRequest = { showExportDialog = false }, title = { Text("Export inventory") }, text = { Text("Choose a file format.") },
        confirmButton = { TextButton(onClick = { pendingFormat = ExportFormat.XLSX; showExportDialog = false; createDocument.launch("resale-inventory-${LocalDate.now()}.xlsx") }) { Text("Excel (.xlsx)") } },
        dismissButton = { TextButton(onClick = { pendingFormat = ExportFormat.CSV; showExportDialog = false; createDocument.launch("resale-inventory-${LocalDate.now()}.csv") }) { Text("CSV") } },
    )
}

@Composable private fun InventoryRow(item: InventoryItem, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) { Text(item.title, style = MaterialTheme.typography.titleMedium); Text("${item.quantity} × ${String.format(Locale.US, "$%.2f", item.askingPriceCents / 100.0)} • ${item.status.name.replace('_', ' ')}", color = MaterialTheme.colorScheme.onSurfaceVariant); if (item.barcode.isNotBlank()) Text(item.barcode, style = MaterialTheme.typography.bodySmall) }
            IconButton(onDelete) { Icon(Icons.Outlined.Delete, "Delete") }
        }
    }
}
