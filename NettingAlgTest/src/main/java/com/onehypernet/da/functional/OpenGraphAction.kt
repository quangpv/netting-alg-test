package com.onehypernet.da.functional

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList
import com.brunomnsilva.smartgraph.graph.Graph
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties
import com.onehypernet.da.controller.IGraphItem
import com.onehypernet.da.core.StageLoader
import com.onehypernet.da.widget.SmartGraphDemoContainer
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import javafx.stage.StageStyle


class OpenGraphAction(
    private val title: String,
    private val payments: () -> List<IGraphItem>
) : EventHandler<MouseEvent>, StageLoader.OnStartListener {

    companion object {
        private val javaClass get() = OpenGraphAction::class.java
        private val css = javaClass.getResource("/css/smartgraph.css")?.toURI()
        private val properties = SmartGraphProperties(javaClass.getResourceAsStream("/smartgraph.properties"))
    }

    private val stage = Stage(StageStyle.DECORATED).also {
        it.title = title
    }

    override fun handle(event: MouseEvent?) {
        val payments = payments()

        val g: Graph<String, String> = DigraphEdgeList()
        val vertex = arrayListOf<String>()

        payments.forEach {
            vertex.add(it.fromPartyId)
            vertex.add(it.toPartyId)
        }

        vertex.toSet().forEach { g.insertVertex(it) }

        payments.forEachIndexed { index, it ->
            try {
                g.insertEdge(it.fromPartyId, it.toPartyId, "${it.amount} ${it.currency} ($index)")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        val strategy = SmartCircularSortedPlacementStrategy()
        val graphView: SmartGraphPanel<String, String> = SmartGraphPanel(g, properties, strategy, css)
        val scene = Scene(SmartGraphDemoContainer(graphView), 1024.0, 768.0)
        stage.scene = scene
        stage.show()

        graphView.init()
    }
}
