package com.onehypernet.da.views.widget

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.TableColumn

class PTableColumn<S, T>(text: String?) : TableColumn<S, T>(text) {
    private val percentageWidth: DoubleProperty = SimpleDoubleProperty(1.0)

    init {
        tableViewProperty().addListener { _, _, t1 ->
            if (prefWidthProperty().isBound) {
                prefWidthProperty().unbind()
            }
            prefWidthProperty().bind(t1.widthProperty().multiply(percentageWidth))
        }
    }

    fun percentageWidthProperty(): DoubleProperty {
        return percentageWidth
    }

    fun getPercentageWidth(): Double {
        return percentageWidthProperty().get()
    }

    fun setPercentageWidth(value: Double) {
        if (value in 0.0..1.0) {
            percentageWidthProperty().set(value)
        } else {
            throw IllegalArgumentException(
                String.format("The provided percentage width is not between 0.0 and 1.0. Value is: %1\$s", value)
            )
        }
    }
}