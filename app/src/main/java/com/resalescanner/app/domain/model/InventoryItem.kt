package com.resalescanner.app.domain.model

import java.time.Instant

data class InventoryItem(
    val id: Long = 0,
    val barcode: String = "",
    val title: String,
    val description: String = "",
    val purchasePriceCents: Long = 0,
    val askingPriceCents: Long = 0,
    val quantity: Int = 1,
    val status: InventoryStatus = InventoryStatus.IN_STOCK,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

enum class InventoryStatus { IN_STOCK, LISTED, SOLD }

