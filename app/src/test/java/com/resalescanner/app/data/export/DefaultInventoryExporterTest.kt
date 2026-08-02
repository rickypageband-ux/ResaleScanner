package com.resalescanner.app.data.export

import com.resalescanner.app.domain.model.InventoryItem
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultInventoryExporterTest {
    private val exporter = DefaultInventoryExporter()
    private val item = InventoryItem(id = 1, barcode = "012345", title = "Vintage, \"Lamp\"", purchasePriceCents = 1250, askingPriceCents = 2999)

    @Test fun csvEscapesValues() {
        val output = ByteArrayOutputStream()
        exporter.export(listOf(item), ExportFormat.CSV, output)
        assertTrue(output.toString().contains("\"Vintage, \"\"Lamp\"\"\""))
    }

    @Test fun xlsxContainsRequiredParts() {
        val output = ByteArrayOutputStream()
        exporter.export(listOf(item), ExportFormat.XLSX, output)
        val entries = buildList {
            ZipInputStream(output.toByteArray().inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) { add(entry.name); entry = zip.nextEntry }
            }
        }
        assertEquals(setOf("[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/_rels/workbook.xml.rels", "xl/worksheets/sheet1.xml"), entries.toSet())
    }
}
