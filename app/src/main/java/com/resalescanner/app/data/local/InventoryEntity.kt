package com.resalescanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.resalescanner.app.domain.model.InventoryItem
import com.resalescanner.app.domain.model.InventoryStatus
import java.time.Instant

@Entity(tableName = "inventory_items")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val title: String,
    val description: String,
    val purchasePriceCents: Long,
    val askingPriceCents: Long,
    val quantity: Int,
    val status: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

fun InventoryEntity.toDomain() = InventoryItem(
    id = id,
    barcode = barcode,
    title = title,
    description = description,
    purchasePriceCents = purchasePriceCents,
    askingPriceCents = askingPriceCents,
    quantity = quantity,
    status = runCatching { InventoryStatus.valueOf(status) }.getOrDefault(InventoryStatus.IN_STOCK),
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
)

fun InventoryItem.toEntity() = InventoryEntity(
    id = id,
    barcode = barcode.trim(),
    title = title.trim(),
    description = description.trim(),
    purchasePriceCents = purchasePriceCents.coerceAtLeast(0),
    askingPriceCents = askingPriceCents.coerceAtLeast(0),
    quantity = quantity.coerceAtLeast(1),
    status = status.name,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
)

