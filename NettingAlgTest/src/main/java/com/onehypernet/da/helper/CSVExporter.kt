@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.onehypernet.da.helper

import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriter
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvWriterSettings
import java.io.File

object CSVExporter {
    fun <T : CSVRecord> write(file: File, value: List<T>) {
        val title = value.firstOrNull()?.title
        val writer = CsvWriter(file, CsvWriterSettings())
        writer.writeHeaders(title)
        writer.writeStringRows(value.map { it.entry })
        writer.close()
    }
}

interface CSVRecord {
    val entry: List<String>
    val title: List<String>
}