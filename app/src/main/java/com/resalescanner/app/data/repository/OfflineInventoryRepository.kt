package com.resalescanner.app.data.repository

import com.resalescanner.app.data.local.InventoryDao
import com.resalescanner.app.data.local.toDomain
import com.resalescanner.app.data.local.toEntity
import com.resalescanner.app.domain.model.InventoryItem
import com.resalescanner.app.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineInventoryRepository(private val dao: InventoryDao) : InventoryRepository {
    override fun observeAll(): Flow<List<InventoryItem>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override fun observeById(id: Long): Flow<InventoryItem?> = dao.observeById(id).map { it?.toDomain() }
    override suspend fun save(item: InventoryItem): Long = dao.upsert(item.toEntity())
    override suspend fun delete(item: InventoryItem) = dao.delete(item.toEntity())
}

