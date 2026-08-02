package com.resalescanner.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resalescanner.app.domain.model.InventoryStatus
import com.resalescanner.app.ui.AppViewModel
import java.util.Locale

@Composable
fun HomeScreen(viewModel: AppViewModel, onScan: () -> Unit, onInventory: () -> Unit, onAdd: () -> Unit) {
    val items by viewModel.inventory.collectAsState()
    val investment = items.sumOf { it.purchasePriceCents * it.quantity }
    val projected = items.filter { it.status != InventoryStatus.SOLD }.sumOf { (it.askingPriceCents - it.purchasePriceCents) * it.quantity }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Good finds start here", style = MaterialTheme.typography.headlineMedium)
        Text("Scan an item or manage your inventory.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onScan, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.QrCodeScanner, null); Text("  Scan barcode") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onAdd, Modifier.weight(1f)) { Icon(Icons.Outlined.Add, null); Text(" Add item") }
            OutlinedButton(onInventory, Modifier.weight(1f)) { Icon(Icons.Outlined.Inventory2, null); Text(" Inventory") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Items", items.sumOf { it.quantity }.toString(), Modifier.weight(1f))
            MetricCard("Invested", money(investment), Modifier.weight(1f))
        }
        MetricCard("Projected gross profit", money(projected), Modifier.fillMaxWidth())
    }
}

@Composable private fun MetricCard(label: String, value: String, modifier: Modifier) {
    Card(modifier) { Column(Modifier.padding(16.dp)) { Text(label, style = MaterialTheme.typography.labelLarge); Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) } }
}

private fun money(cents: Long) = String.format(Locale.US, "$%,.2f", cents / 100.0)
