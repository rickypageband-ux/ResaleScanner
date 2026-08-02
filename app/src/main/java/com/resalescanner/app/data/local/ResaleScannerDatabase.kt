package com.resalescanner.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [InventoryEntity::class], version = 1, exportSchema = true)
abstract class ResaleScannerDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
}

