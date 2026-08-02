package com.resalescanner.app.domain.repository

import com.resalescanner.app.domain.model.InventoryItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun observeAll(): Flow<List<InventoryItem>>
    fun observeById(id: Long): Flow<InventoryItem?>
    suspend fun save(item: InventoryItem): Long
    suspend fun delete(item: InventoryItem)
}

