package com.onehypernet.da.views.widget

import com.onehypernet.da.app.R
import com.onehypernet.da.app.Style
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.util.Callback

class SimpleTableCellFactory<T> : Callback<TableColumn<T, String>, TableCell<T, String>> {
    override fun call(param: TableColumn<T, String>?): TableCell<T, String> {
        return Cell<T>()
    }

    class Cell<T> : TableCell<T, String>() {
        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null) {
                text = null
                Style.setBackgroundColor(this, R.color.white)
            } else {
                text = item
            }
        }
    }
}