package com.onehypernet.da.controller

import com.onehypernet.da.view.TableAdapter
import javafx.scene.control.TableView

data class NettingParams(
    override val fx1: String = "",
    override val fx2: String = "",
    override val margin: String = "",
    override val feePercent: String = "",
    override val feeMin: String = "",
    override val feeMax: String = "",
    override val fixedFee: String = "",
    override val exchangeRate: String = "",
    override val location: String = "",
    override val toLocations: String = ""
) : INettingParams

interface INettingParams {
    fun isNotBlank(): Boolean {
        return arrayOf(fx1, fx2, feePercent, fixedFee, exchangeRate, location).all { it.isNotBlank() }
    }

    val fx1: String
    val fx2: String
    val margin: String
    val feePercent: String
    val feeMin: String
    val feeMax: String
    val fixedFee: String
    val exchangeRate: String
    val location: String
    val toLocations: String
}

class NettingParamAdapter(private val view: TableView<INettingParams>) : TableAdapter<INettingParams>(
    view, listOf(
        Column("FX1", "fx1"),
        Column("FX2", "fx2"),
        Column("Margin(%)", "margin"),
        Column("Fee(%)", "feePercent"),
        Column("Min", "feeMin"),
        Column("Max", "feeMax"),
        Column("Fixed(USD)", "fixedFee"),
        Column("FX", "exchangeRate"),
        Column("Location", "location"),
        Column("To Locations", "toLocations"),
    )
)
