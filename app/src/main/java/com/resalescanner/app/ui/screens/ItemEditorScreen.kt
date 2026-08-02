package com.resalescanner.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.resalescanner.app.domain.model.InventoryItem
import com.resalescanner.app.ui.AppViewModel
import com.resalescanner.app.ui.ItemDraft
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorScreen(viewModel: AppViewModel, itemId: Long?, initialBarcode: String, onBack: () -> Unit) {
    val inventory by viewModel.inventory.collectAsState()
    var draft by remember(itemId, initialBarcode) { mutableStateOf(ItemDraft(barcode = initialBarcode)) }
    var loaded by remember(itemId) { mutableStateOf(false) }
    LaunchedEffect(itemId, inventory) {
        if (itemId != null && !loaded) inventory.firstOrNull { it.id == itemId }?.let { draft = it.toDraft(); loaded = true }
    }
    Scaffold(topBar = { TopAppBar(title = { Text(if (itemId == null) "Add item" else "Edit item") }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(draft.title, { draft = draft.copy(title = it) }, label = { Text("Item title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(draft.barcode, { draft = draft.copy(barcode = it) }, label = { Text("Barcode") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(draft.description, { draft = draft.copy(description = it) }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(draft.purchasePrice, { draft = draft.copy(purchasePrice = it) }, label = { Text("Cost") }, prefix = { Text("$") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(draft.askingPrice, { draft = draft.copy(askingPrice = it) }, label = { Text("Asking") }, prefix = { Text("$") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
            OutlinedTextField(draft.quantity, { draft = draft.copy(quantity = it.filter(Char::isDigit)) }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            Button(onClick = { viewModel.save(draft, onBack) }, enabled = draft.title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Save item") }
        }
    }
}

private fun InventoryItem.toDraft() = ItemDraft(id, barcode, title, description, purchasePriceCents.moneyInput(), askingPriceCents.moneyInput(), quantity.toString(), status, createdAt)
private fun Long.moneyInput() = String.format(Locale.US, "%.2f", this / 100.0)
