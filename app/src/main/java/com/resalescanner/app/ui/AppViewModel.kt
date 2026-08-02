package com.resalescanner.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.resalescanner.app.data.export.ExportFormat
import com.resalescanner.app.data.export.InventoryExporter
import com.resalescanner.app.domain.model.InventoryItem
import com.resalescanner.app.domain.model.InventoryStatus
import com.resalescanner.app.domain.repository.InventoryRepository
import java.io.OutputStream
import java.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: InventoryRepository,
    private val exporter: InventoryExporter,
) : ViewModel() {
    val inventory: StateFlow<List<InventoryItem>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(draft: ItemDraft, onSaved: () -> Unit = {}) {
        if (draft.title.isBlank()) return
        viewModelScope.launch {
            repository.save(draft.toItem())
            onSaved()
        }
    }

    fun delete(item: InventoryItem) = viewModelScope.launch { repository.delete(item) }

    fun export(format: ExportFormat, output: OutputStream) {
        exporter.export(inventory.value, format, output)
    }

    class Factory(
        private val repository: InventoryRepository,
        private val exporter: InventoryExporter,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(repository, exporter) as T
    }
}

data class ItemDraft(
    val id: Long = 0,
    val barcode: String = "",
    val title: String = "",
    val description: String = "",
    val purchasePrice: String = "",
    val askingPrice: String = "",
    val quantity: String = "1",
    val status: InventoryStatus = InventoryStatus.IN_STOCK,
    val createdAt: Instant = Instant.now(),
) {
    fun toItem() = InventoryItem(
        id = id,
        barcode = barcode,
        title = title,
        description = description,
        purchasePriceCents = purchasePrice.toCents(),
        askingPriceCents = askingPrice.toCents(),
        quantity = quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        status = status,
        createdAt = createdAt,
        updatedAt = Instant.now(),
    )
}

private fun String.toCents(): Long = ((toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO) * java.math.BigDecimal(100)).toLong().coerceAtLeast(0)
