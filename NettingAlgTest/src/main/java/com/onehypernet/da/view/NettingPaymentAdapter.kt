package com.onehypernet.da.view

import com.onehypernet.da.controller.IGraphItem
import com.onehypernet.da.helper.CSVRecord
import com.onehypernet.da.helper.textFormatter
import com.onehypernet.model.NettingPayment
import com.onehypernet.netting.optimize.ParameterLookup
import javafx.scene.control.TableView

class NettingPaymentHolder(private val payment: NettingPayment, lookup: ParameterLookup) : IGraphItem, CSVRecord {
    private val params = lookup.getNetting(payment.fromPartyId, payment.toPartyId, payment.currency)

    override val fromPartyId: String = payment.fromPartyId
    override val toPartyId: String = payment.toPartyId
    override val amount: String = textFormatter.formatAmount(payment.amount)
    val margin: String = params.marginPercent.toString()
    val rateFee: String = params.feePercent.toString()
    val min: String = params.minFee.toString()
    val max: String = params.maxFee.toString()
    val fixedFee: String = params.fixedFee.toString()
    override val currency: String = payment.currency

    override val entry: List<String>
        get() = listOf(fromPartyId, toPartyId, payment.amount.toString(), currency)

    override val title: List<String>
        get() = listOf("From", "To", "Amount", "Currency")
}

class NettingPaymentAdapter(view: TableView<NettingPaymentHolder>) : TableAdapter<NettingPaymentHolder>(
    view, listOf(
        Column("From", "fromPartyId"),
        Column("To", "toPartyId"),
        Column("Amount", "amount"),
        Column("Currency", "currency"),
        Column("M%", "margin"),
        Column("F%", "rateFee"),
        Column("Min", "min"),
        Column("Max", "max"),
        Column("Fixed(USD)", "fixedFee"),
    )
)