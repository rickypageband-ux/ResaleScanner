package com.resalescanner.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.resalescanner.app.domain.model.InventoryStatus
import com.resalescanner.app.ui.AppViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onScan: () -> Unit,
    onTakePicture: () -> Unit,
    onSearch: () -> Unit,
    onInventory: () -> Unit,
    onProfitCalculator: () -> Unit,
    onExport: () -> Unit,
) {
    val items by viewModel.inventory.collectAsState()
    val projected = items.filter { it.status != InventoryStatus.SOLD }
        .sumOf { (it.askingPriceCents - it.purchasePriceCents) * it.quantity }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Resale Scanner", style = MaterialTheme.typography.headlineMedium)
        Text("What would you like to do?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        HomeAction("Scan Barcode", Icons.Outlined.QrCodeScanner, onScan)
        HomeAction("Take Picture", Icons.Outlined.CameraAlt, onTakePicture)
        HomeAction("Search by Name", Icons.Outlined.Search, onSearch)
        HomeAction("My Inventory", Icons.Outlined.Inventory2, onInventory)
        HomeAction("Profit Calculator", Icons.Outlined.Calculate, onProfitCalculator)
        HomeAction("Export Inventory", Icons.Outlined.FileUpload, onExport)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Items", items.sumOf { it.quantity }.toString(), Modifier.weight(1f))
            MetricCard("Projected profit", money(projected), Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth()) {
        Icon(icon, null)
        Text("  $label", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun money(cents: Long) = String.format(Locale.US, "$%,.2f", cents / 100.0)
