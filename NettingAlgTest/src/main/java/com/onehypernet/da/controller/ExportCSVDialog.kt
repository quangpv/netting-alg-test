package com.onehypernet.da.controller

import com.onehypernet.da.helper.CSVExporter
import com.onehypernet.da.helper.CSVRecord
import com.onehypernet.da.widget.ErrorDialog
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
import java.io.File


class ExportCSVDialog(private val node: Node) {

    private var chooser = FileChooser()

    init {
        chooser.title = "Save netting params"
    }

    fun show(values: List<CSVRecord>) {
        if (values.isEmpty()) return
        var file = chooser.showSaveDialog(node.scene.window) ?: return
        if (file.extension != "csv")
            file = File(file.parent, "${file.nameWithoutExtension}.csv")
        CSVExporter.write(file, values)
    }

    class Action(private val node: Node, private val factory: () -> List<CSVRecord>?) : EventHandler<MouseEvent> {
        override fun handle(event: MouseEvent?) {
            val result = factory()
            if (result == null || result.isEmpty()) {
                ErrorDialog().show("No data to export")
                return
            }
            ExportCSVDialog(node).show(result)
        }
    }
}