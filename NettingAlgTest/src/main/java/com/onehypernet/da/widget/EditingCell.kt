package com.onehypernet.da.widget

import javafx.scene.control.TableCell
import javafx.scene.control.TextField

class EditingCell<T> : TableCell<T, String>() {
    private lateinit var textField: TextField

    override fun startEdit() {
        if (!isEmpty) {
            super.startEdit()
            createTextField()
            text = null
            graphic = textField
            textField.selectAll()
        }
    }

    override fun cancelEdit() {
        super.cancelEdit()
        text = item
        graphic = null
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            graphic = null
        } else {
            if (isEditing) {
                textField.text = string
                text = null
                graphic = textField
            } else {
                text = string
                graphic = null
            }
        }
    }

    private fun createTextField() {
        textField = TextField(string)
        textField.minWidth = this.width - this.graphicTextGap * 2
        textField.focusedProperty().addListener { _, _, arg2 ->
            if (!arg2!!) {
                commitEdit(textField.text)
            }
        }
    }

    private val string: String
        get() = if (item == null) "" else item.toString()
}