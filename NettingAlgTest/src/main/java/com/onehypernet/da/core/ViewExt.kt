package com.onehypernet.da.core

import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label


fun <T : Node> Parent.findById(id: String): T {
    return findNodeById(id) ?: error("Not found $id")
}

@Suppress("unchecked_cast")
private fun <T : Node> Parent.findNodeById(id: String): T? {
    var founded: T?
    for (node in this.childrenUnmodifiable) {
        if (node.id == id) return node as T
        if (node is Parent) {
            founded = node.findNodeById(id)
            if (founded != null) return founded
        }
    }
    return null
}

fun Label.visible(isVisible: Boolean = true, function: Label.() -> Unit = {}) {
    if (isVisible) function()
    this.isVisible = isVisible
}

@Suppress("unchecked_cast")
fun <T : Node> Parent.child(i: Int): T {
    return childrenUnmodifiable[i] as T
}

fun Node.findController(): Controller {
    val key = "javafx:controller"
    if (properties.containsKey(key)) return properties[key] as Controller
    val parent = this.parent ?: error("Not found controller")
    return parent.findController()
}
