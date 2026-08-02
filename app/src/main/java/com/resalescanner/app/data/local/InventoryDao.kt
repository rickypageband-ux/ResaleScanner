package com.resalescanner.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    fun observeById(id: Long): Flow<InventoryEntity?>

    @Upsert
    suspend fun upsert(item: InventoryEntity): Long

    @Delete
    suspend fun delete(item: InventoryEntity)
}

