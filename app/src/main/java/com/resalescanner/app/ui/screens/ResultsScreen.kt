package com.resalescanner.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.resalescanner.app.domain.model.sampleSearchResult
import com.resalescanner.app.ui.AppViewModel
import com.resalescanner.app.ui.ItemDraft
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(viewModel: AppViewModel, query: String, onBack: () -> Unit, onAdded: () -> Unit) {
    val result = remember(query) { sampleSearchResult(query) }
    var purchasePrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    val purchaseCents = ((purchasePrice.toDoubleOrNull() ?: 0.0) * 100).toLong()
    val quantityValue = quantity.toIntOrNull()?.takeIf { it > 0 }
    val profitCents = (result.suggestedResalePriceCents - purchaseCents) * (quantityValue ?: 1)
    val canAdd = purchasePrice.toDoubleOrNull()?.let { it >= 0 } == true && quantityValue != null

    Scaffold(topBar = { TopAppBar(title = { Text("Results") }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(result.title, style = MaterialTheme.typography.headlineMedium)
            if (result.isSampleData) Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) { Text("Sample prices - live provider connections are not configured", Modifier.padding(10.dp), style = MaterialTheme.typography.labelMedium) }
            Text("Retail Prices", style = MaterialTheme.typography.titleLarge)
            result.retailPrices.forEach { PriceRow(it.retailer, it.priceCents) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("Lowest", result.lowestPriceCents, Modifier.weight(1f))
                SummaryCard("Highest", result.highestPriceCents, Modifier.weight(1f))
                SummaryCard("Average", result.averagePriceCents, Modifier.weight(1f))
            }
            PriceRow("Estimated eBay sold price", result.estimatedSoldPriceCents, emphasized = true)
            PriceRow("Suggested resale price", result.suggestedResalePriceCents, emphasized = true)
            OutlinedTextField(purchasePrice, { purchasePrice = it }, label = { Text("How much did you pay?") }, prefix = { Text("$") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(quantity, { quantity = it.filter(Char::isDigit) }, label = { Text("Quantity purchased") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            if (purchasePrice.isNotBlank()) Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Estimated total profit"); Text(money(profitCents), style = MaterialTheme.typography.headlineMedium, color = if (profitCents >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error); Text("For ${quantityValue ?: 1} item(s), before marketplace fees and shipping", style = MaterialTheme.typography.bodySmall) } }
            Button(
                onClick = {
                    viewModel.save(
                        ItemDraft(
                            barcode = query.takeIf { it.length in 8..14 && it.all(Char::isDigit) }.orEmpty(),
                            title = result.title,
                            description = "Added from product search: $query",
                            purchasePrice = purchasePrice,
                            askingPrice = String.format(Locale.US, "%.2f", result.suggestedResalePriceCents / 100.0),
                            quantity = quantity,
                        ),
                        onAdded,
                    )
                },
                enabled = canAdd,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Add to My Inventory") }
            if (!canAdd) Text("Enter your purchase price and a quantity of at least 1 to add this item.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun PriceRow(label: String, cents: Long, emphasized: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge); Text(money(cents), style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge, color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
}

@Composable private fun SummaryCard(label: String, cents: Long, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(10.dp)) { Text(label, style = MaterialTheme.typography.labelMedium); Text(money(cents), style = MaterialTheme.typography.titleMedium) } } }
private fun money(cents: Long) = String.format(Locale.US, "$%,.2f", cents / 100.0)
