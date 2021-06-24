package com.onehypernet.da.app

import javafx.scene.Node

object Style {
    fun setTextColor(view: Node, color: String) {
        view.style = "-fx-text-fill: $color"
    }

    fun addClass(view: Node, className: String) {
        view.styleClass.add(className)
    }

    fun removeClass(view: Node, className: String) {
        view.styleClass.remove(className)
    }

    fun setBackgroundColor(node: Node, color: String) {
        node.style = "-fx-background-color: $color"
    }
}