package com.onehypernet.da.widget

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
import java.io.File

class ImportCSVAction(private val node: Node, private val title: String, private val callback: (File) -> Unit) :
    EventHandler<MouseEvent> {
    private val filters: List<FileChooser.ExtensionFilter>
        get() = listOf(
            FileChooser.ExtensionFilter(title, "*.csv")
        )

    private val fileChooser by lazy(LazyThreadSafetyMode.NONE) {
        FileChooser().apply { onCreateChooser(this) }
    }

    private fun onCreateChooser(fileChooser: FileChooser) {
        fileChooser.title = "Open Resource File"
        fileChooser.extensionFilters.addAll(filters)
    }

    override fun handle(event: MouseEvent?) {
        val file = fileChooser.showOpenDialog(node.scene.window) ?: return
        callback(file)
    }

}