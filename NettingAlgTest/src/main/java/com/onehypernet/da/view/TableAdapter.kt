package com.onehypernet.da.view

import com.onehypernet.da.app.R
import com.onehypernet.da.core.LifecycleObserver
import com.onehypernet.da.core.findController
import com.onehypernet.da.views.widget.PTableColumn
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory

abstract class TableAdapter<T : Any>(
    private val view: TableView<T>,
    private val columnFields: List<Column<T>>
) {

    private var mItems: ObservableList<T>? = null
        get() {
            if (field == null) {
                field = FXCollections.observableArrayList()
                createColumns()
                createPlaceHolder()
            }
            return field
        }

    val items get() = mItems!!

    init {
        view.findController().lifecycle.addObserver(object : LifecycleObserver {
            override fun onCreate() {
                view.items = items
            }
        })
    }

    open fun submit(it: List<T>?) {
        items.clear()
        if (it != null) items.addAll(it)
    }

    private fun createPlaceHolder() {
        val holder = onCreatePlaceHolder()
        if (holder != null) view.placeholder = onCreatePlaceHolder()
    }

    private fun createColumns() {
        val columns = view.columns
        columns.clear()

        var numOfRemainPercent = 0
        var totalPercent = 0.0

        for (columnField in columnFields) {
            if (columnField.percent > 0) {
                totalPercent += columnField.percent
            } else {
                numOfRemainPercent++
            }
        }
        val remainPercent = 1 - totalPercent

        val averageRemainPercent = remainPercent / numOfRemainPercent

        for (field in columnFields) {
            columns.add(onCreateColumn(field, averageRemainPercent))
        }

    }

    protected open fun onCreatePlaceHolder(): Node? = Label(R.string.notAvailable)

    protected open fun onCreateColumn(column: Column<T>, averageRemainPercent: Double): TableColumn<T, String> {
        return PTableColumn<T, String>(column.name).apply {
            when {
                column.percent <= 0.0 -> setPercentageWidth(averageRemainPercent)
                else -> setPercentageWidth(column.percent)
            }
            cellValueFactory = PropertyValueFactory(column.field)
        }
    }

    class Column<T : Any>(
        val name: String,
        val field: String,
        val percent: Double = -1.0
    ) {
        operator fun invoke(obj: T, value: String) {
            obj.javaClass.getDeclaredField(field).apply {
                isAccessible = true
            }.set(obj, value)
        }
    }
}