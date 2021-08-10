package com.onehypernet.da.view

import javafx.scene.control.TableView

class Location(
    var partyId: String,
    var locationCode: String,
    var currencyCode: String,
    var selfConvert: String,
)

class LocationAdapter(private val view: TableView<Location>) : TableAdapter<Location>(
    view, listOf(
        Column("PartyId", "partyId"),
        Column("Location Code", "locationCode"),
        Column("Currency", "currencyCode"),
        Column("Self", "selfConvert"),
    )
)