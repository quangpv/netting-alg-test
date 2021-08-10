package com.onehypernet.da.widget

import com.brunomnsilva.smartgraph.containers.ContentResizerPane
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel
import javafx.scene.control.CheckBox
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox

class SmartGraphDemoContainer(graphView: SmartGraphPanel<*, *>) : BorderPane() {
    init {
        center = ContentResizerPane(graphView)

        //create bottom pane with controls
        val bottom = HBox(10.0)
        val automatic = CheckBox("Automatic layout")
        automatic.selectedProperty().bindBidirectional(graphView.automaticLayoutProperty())
        bottom.children.add(automatic)
        setBottom(bottom)
    }
}