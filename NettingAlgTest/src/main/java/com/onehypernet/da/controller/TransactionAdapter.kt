package com.onehypernet.da.controller

import com.onehypernet.da.helper.CSVRecord
import com.onehypernet.da.view.TableAdapter
import com.onehypernet.da.widget.EditingCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView

interface ITransaction : IGraphItem, CSVRecord {
    val convertible: String

    override val entry: List<String>
        get() = listOf(fromPartyId, toPartyId, amount, currency, convertible)
    override val title: List<String>
        get() = listOf("From", "To", "Amount", "Currency", "Convertible")
}

data class Transaction(
    override var fromPartyId: String = "",
    override var toPartyId: String = "",
    override var amount: String = "",
    override var currency: String = "",
    override var convertible: String = ""
) : ITransaction

class TransactionAdapter(private val view: TableView<ITransaction>) : TableAdapter<ITransaction>(
    view, listOf(
        Column("From", "fromPartyId"),
        Column("To", "toPartyId"),
        Column("Amount", "amount"),
        Column("Currency", "currency"),
        Column("Convertible", "convertible"),
    )
) {
    var onItemsChangedListener: (List<ITransaction>) -> Unit = {}

    fun addEmpty() {
        view.items.add(Transaction())
        onItemsChangedListener(view.items)
    }

    fun removeSelected() {
        view.items.remove(view.focusModel.focusedItem)
        onItemsChangedListener(view.items)
    }

    override fun submit(it: List<ITransaction>?) {
        super.submit(it)
        onItemsChangedListener(view.items)
    }

    override fun onCreateColumn(
        column: Column<ITransaction>,
        averageRemainPercent: Double
    ): TableColumn<ITransaction, String> {
        return super.onCreateColumn(column, averageRemainPercent).apply {
            setCellFactory { EditingCell() }
            setOnEditCommit { column(items[it.tablePosition.row], it.newValue) }
        }
    }

    init {
        view.isEditable = true
    }
}