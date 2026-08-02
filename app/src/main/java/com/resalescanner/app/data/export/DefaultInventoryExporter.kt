package com.resalescanner.app.data.export

import com.resalescanner.app.domain.model.InventoryItem
import java.io.OutputStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DefaultInventoryExporter : InventoryExporter {
    private val headers = listOf("ID", "Barcode", "Title", "Description", "Purchase price", "Asking price", "Quantity", "Status", "Updated")
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())

    override fun export(items: List<InventoryItem>, format: ExportFormat, output: OutputStream) {
        when (format) {
            ExportFormat.CSV -> writeCsv(items, output)
            ExportFormat.XLSX -> writeXlsx(items, output)
        }
    }

    private fun rows(items: List<InventoryItem>): List<List<String>> = listOf(headers) + items.map {
        listOf(it.id.toString(), it.barcode, it.title, it.description, cents(it.purchasePriceCents), cents(it.askingPriceCents), it.quantity.toString(), it.status.name, dateFormat.format(it.updatedAt))
    }

    private fun writeCsv(items: List<InventoryItem>, output: OutputStream) {
        output.bufferedWriter().use { writer ->
            rows(items).forEach { row ->
                writer.appendLine(row.joinToString(",") { value -> "\"${value.replace("\"", "\"\"")}\"" })
            }
        }
    }

    private fun writeXlsx(items: List<InventoryItem>, output: OutputStream) {
        ZipOutputStream(output.buffered()).use { zip ->
            zip.entry("[Content_Types].xml", contentTypes)
            zip.entry("_rels/.rels", rootRelationships)
            zip.entry("xl/workbook.xml", workbook)
            zip.entry("xl/_rels/workbook.xml.rels", workbookRelationships)
            val xmlRows = rows(items).mapIndexed { rowIndex, row ->
                val cells = row.mapIndexed { columnIndex, value ->
                    val ref = "${columnName(columnIndex)}${rowIndex + 1}"
                    "<c r=\"$ref\" t=\"inlineStr\"><is><t>${xml(value)}</t></is></c>"
                }.joinToString("")
                "<row r=\"${rowIndex + 1}\">$cells</row>"
            }.joinToString("")
            zip.entry("xl/worksheets/sheet1.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>$xmlRows</sheetData></worksheet>")
        }
    }

    private fun ZipOutputStream.entry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun cents(value: Long) = "%.2f".format(java.util.Locale.US, value / 100.0)
    private fun xml(value: String) = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    private fun columnName(index: Int): String = buildString {
        var current = index
        do {
            insert(0, ('A'.code + current % 26).toChar())
            current = current / 26 - 1
        } while (current >= 0)
    }

    private val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>"""
    private val rootRelationships = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""
    private val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Inventory" sheetId="1" r:id="rId1"/></sheets></workbook>"""
    private val workbookRelationships = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>"""
}

