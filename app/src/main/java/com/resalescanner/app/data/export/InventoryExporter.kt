package com.resalescanner.app.data.export

import com.resalescanner.app.domain.model.InventoryItem
import java.io.OutputStream

enum class ExportFormat(val mimeType: String, val extension: String) {
    CSV("text/csv", "csv"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
}

interface InventoryExporter {
    fun export(items: List<InventoryItem>, format: ExportFormat, output: OutputStream)
}

