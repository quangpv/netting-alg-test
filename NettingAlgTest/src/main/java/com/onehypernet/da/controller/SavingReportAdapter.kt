package com.onehypernet.da.controller

import com.onehypernet.da.view.TableAdapter
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView

interface ISavingReport {
    val counterPartyId: String

    val preFee: String
    val prePaymentCount: String
    val preCashTransfer: String

    val postFee: String
    val postPaymentCount: String
    val postCashTransfer: String

    val feeSaving: String
    val noSaving: String
    val cashTransferSaving: String
}


class SavingReportAdapter(view: TableView<ISavingReport>) : TableAdapter<ISavingReport>(
    view, listOf(
        Column("Counter Party", "counterPartyId", 0.1),
        Column("Pre Fee", "preFee", 0.08),
        Column("Post Fee", "postFee", 0.08),
        Column("Fee saving", "feeSaving", 0.09),

        Column("Pre No", "prePaymentCount", 0.05),
        Column("Post No", "postPaymentCount", 0.05),
        Column("No saving", "noSaving", 0.07),

        Column("Pre Transfer", "preCashTransfer"),
        Column("Post Transfer", "postCashTransfer"),
        Column("Transfer saving", "cashTransferSaving"),
    )
) {

    private val mTransferColumns = hashMapOf<String, TableColumn<ISavingReport, String>>()

    fun setCurrency(currency: String) {
        mTransferColumns.forEach { (name, column) ->
            column.text = "${name}($currency)"
        }
    }

    override fun onCreatePlaceHolder(): Node {
        return Label("Please select a party in the table above to view detail")
    }

    override fun onCreateColumn(
        column: Column<ISavingReport>,
        averageRemainPercent: Double
    ): TableColumn<ISavingReport, String> {


        return super.onCreateColumn(column, averageRemainPercent).also {
            if (column.field == "preCashTransfer"
                || column.field == "postCashTransfer"
                || column.field == "cashTransferSaving"
            ) {
                mTransferColumns[column.name] = it
            }
        }
    }
}