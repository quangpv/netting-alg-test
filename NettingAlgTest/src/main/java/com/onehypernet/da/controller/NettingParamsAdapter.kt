package com.onehypernet.da.controller

import com.onehypernet.da.view.TableAdapter
import javafx.scene.control.TableView

data class NettingParams(
    override val fx1: String = "",
    override val fx2: String = "",
    override val feePercent: String = "",
    override val fixedFee: String = "",
    override val exchangeRate: String = "",
    override val location: String = "",
) : INettingParams

interface INettingParams {
    fun isNotBlank(): Boolean {
        return arrayOf(fx1, fx2, feePercent, fixedFee, exchangeRate, location).all { it.isNotBlank() }
    }

    val fx1: String
    val fx2: String
    val feePercent: String
    val fixedFee: String
    val exchangeRate: String
    val location: String
}

class NettingParamAdapter(private val view: TableView<INettingParams>) : TableAdapter<INettingParams>(
    view, listOf(
        Column("FX1", "fx1"),
        Column("FX2", "fx2"),
        Column("%", "feePercent"),
        Column("Fixed(USD)", "fixedFee"),
        Column("FX", "exchangeRate"),
        Column("Location", "location"),
    )
)
