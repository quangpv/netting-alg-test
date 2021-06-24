package com.onehypernet.da.helper

import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import java.util.function.UnaryOperator

class NumberFilter(private val textField: TextField) {
    private val numberReg = Regex("[0-9]*")

    init {
        val filter = UnaryOperator<TextFormatter.Change> { change ->
            val text = change.text
            if (text.matches(numberReg)) change else null
        }
        textField.textFormatter = TextFormatter<String>(filter)
    }
}