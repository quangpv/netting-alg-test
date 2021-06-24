package com.onehypernet.da.controller

import com.onehypernet.da.view.TableAdapter
import javafx.scene.control.TableView

class Location(
    var partyId: String,
    var locationCode: String,
)

class LocationAdapter(private val view: TableView<Location>) : TableAdapter<Location>(
    view, listOf(
        Column("PartyId", "partyId"),
        Column("Location Code", "locationCode"),
    )
)