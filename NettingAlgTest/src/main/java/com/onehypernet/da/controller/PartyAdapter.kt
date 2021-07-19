package com.onehypernet.da.controller

import com.onehypernet.da.view.TableAdapter
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.MouseButton


interface IParty {
    val postBalance: Double
    val preBalance: Double
    val counterPartyId: String
    val totalCashTransferSaving: String
    val totalFeeSaving: String
    val totalNoSaving:String
}

class PartyAdapter(view: TableView<IParty>) : TableAdapter<IParty>(
    view, listOf(
        Column("Counter Party", "counterPartyId"),
        Column("Cash transfer saving", "totalCashTransferSaving"),
        Column("Fee saving", "totalFeeSaving"),
        Column("No saving", "totalNoSaving"),
    )
) {
    var onItemClickListener: (IParty) -> Unit = {}

    init {
        view.setRowFactory {
            TableRow<IParty>().apply {
                setOnMouseClicked { event ->
                    if (!isEmpty
                        && event.button == MouseButton.PRIMARY
                    ) {
                        onItemClickListener(item)
                    }
                }
            }
        }
    }

}