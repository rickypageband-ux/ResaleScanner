package com.resalescanner.app.ui.screens

import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.resalescanner.app.data.export.ExportFormat
import com.resalescanner.app.ui.AppViewModel
import java.time.LocalDate
import java.util.Locale
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchByNameScreen(onBack: () -> Unit, onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    Scaffold(topBar = { SimpleTopBar("Search by Name", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("Item name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Search retail listings and comparable sold items by product name.")
            Button({ onSearch(query.trim()) }, enabled = query.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Search online") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitCalculatorScreen(onBack: () -> Unit) {
    var purchase by remember { mutableStateOf("") }
    var sale by remember { mutableStateOf("") }
    var fees by remember { mutableStateOf("") }
    var shipping by remember { mutableStateOf("") }
    val profit = (sale.toDoubleOrNull() ?: 0.0) - (purchase.toDoubleOrNull() ?: 0.0) - (fees.toDoubleOrNull() ?: 0.0) - (shipping.toDoubleOrNull() ?: 0.0)
    Scaffold(topBar = { SimpleTopBar("Profit Calculator", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MoneyField("Purchase price", purchase) { purchase = it }
            MoneyField("Sale price", sale) { sale = it }
            MoneyField("Marketplace fees", fees) { fees = it }
            MoneyField("Shipping cost", shipping) { shipping = it }
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Estimated profit"); Text(String.format(Locale.US, "$%,.2f", profit), style = MaterialTheme.typography.headlineMedium, color = if (profit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportInventoryScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var format by remember { mutableStateOf(ExportFormat.CSV) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { requireNotNull(context.contentResolver.openOutputStream(uri)).use { viewModel.export(format, it) } }
            .onSuccess { Toast.makeText(context, "Inventory exported", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show() }
    }
    Scaffold(topBar = { SimpleTopBar("Export Inventory", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Save a copy of your inventory for spreadsheets, bookkeeping, or backup.")
            Button({ format = ExportFormat.CSV; launcher.launch("resale-inventory-${LocalDate.now()}.csv") }, Modifier.fillMaxWidth()) { Text("Export CSV") }
            OutlinedButton({ format = ExportFormat.XLSX; launcher.launch("resale-inventory-${LocalDate.now()}.xlsx") }, Modifier.fillMaxWidth()) { Text("Export Excel (.xlsx)") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakePictureScreen(onBack: () -> Unit, onSearch: (String) -> Unit) {
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) capturedImage = bitmap
    }
    Scaffold(topBar = { SimpleTopBar("Take Picture", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Icon(Icons.Outlined.CameraAlt, null, tint = MaterialTheme.colorScheme.primary)
            Text("Photograph an item without a barcode", style = MaterialTheme.typography.titleLarge)
            capturedImage?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured item",
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Fit,
                )
                Button(
                    onClick = {
                        val bytes = ByteArrayOutputStream().use { output ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
                            output.toByteArray()
                        }
                        onSearch(Base64.encodeToString(bytes, Base64.NO_WRAP))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Identify item and search prices") }
                OutlinedButton({ camera.launch(null) }, Modifier.fillMaxWidth()) { Text("Retake photo") }
            } ?: run {
                Text("Tools • Toys • Electronics • Shoes • Clothes • Collectibles")
                Button({ camera.launch(null) }, Modifier.fillMaxWidth()) { Text("Open Camera") }
            }
            Text("Your photo is searched against live eBay listings to identify the item and estimate its price.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun MoneyField(label: String, value: String, onValue: (String) -> Unit) = OutlinedTextField(value, onValue, label = { Text(label) }, prefix = { Text("$") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SimpleTopBar(title: String, onBack: () -> Unit) = TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } })
