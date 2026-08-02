package com.resalescanner.app

import android.content.Context
import androidx.room.Room
import com.resalescanner.app.data.export.DefaultInventoryExporter
import com.resalescanner.app.data.export.InventoryExporter
import com.resalescanner.app.data.local.ResaleScannerDatabase
import com.resalescanner.app.data.repository.OfflineInventoryRepository
import com.resalescanner.app.domain.repository.InventoryRepository

interface AppContainer {
    val inventoryRepository: InventoryRepository
    val inventoryExporter: InventoryExporter
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database = Room.databaseBuilder(context, ResaleScannerDatabase::class.java, "resale-scanner.db").build()
    override val inventoryRepository: InventoryRepository = OfflineInventoryRepository(database.inventoryDao())
    override val inventoryExporter: InventoryExporter = DefaultInventoryExporter()
}

