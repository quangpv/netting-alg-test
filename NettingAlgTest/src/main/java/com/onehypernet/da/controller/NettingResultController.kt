package com.onehypernet.da.controller

import com.onehypernet.da.core.Controller
import com.onehypernet.da.core.navigation.ArgumentChangeable
import com.onehypernet.da.helper.textFormatter
import com.onehypernet.model.NettingResult
import com.onehypernet.model.PartyReport
import com.onehypernet.model.SavingReport
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TableView

class NettingResultController : Controller(), ArgumentChangeable {
    private lateinit var partyAdapter: PartyAdapter
    private lateinit var savingReportAdapter: SavingReportAdapter

    lateinit var tbNettingSaving: TableView<ISavingReport>
    lateinit var tbParty: TableView<IParty>

    lateinit var lbTotalSimulatedFee: Label
    lateinit var lbTotalFee: Label
    lateinit var lbSaving: Label

    override fun onCreate(node: Node) {
        super.onCreate(node)
        savingReportAdapter = SavingReportAdapter(tbNettingSaving)
        partyAdapter = PartyAdapter(tbParty)

        partyAdapter.onItemClickListener = { party ->
            lbSaving.text = "Party ${party.counterPartyId} Saving"
            savingReportAdapter.setCurrency(party.currency)
            savingReportAdapter.submit((party as PartyImpl).savings.map { SavingReportImpl(it) })
        }
    }

    @Suppress("unchecked_cast")
    override fun onNewArguments(args: Any) {
        val result = args as? NettingResult ?: return
        lbTotalFee.text = "Total fee: ${textFormatter.formatFee(result.totalFee)} USD"
        lbTotalSimulatedFee.text = "Total simulated fee: ${textFormatter.formatFee(result.totalSimulatedFee)} USD"
        partyAdapter.submit(result.reports.map { PartyImpl(it) })
    }
}

class PartyImpl(private val nettingReport: PartyReport) : IParty {
    override val counterPartyId: String
        get() = nettingReport.partyId

    override val totalCashTransferSaving: String
        get() = textFormatter.formatFee(nettingReport.totalCashTransferSaving, nettingReport.partyCurrency)

    override val totalFeeSaving: String
        get() = textFormatter.formatFee(nettingReport.totalFeeSaving, "USD")
    override val currency: String
        get() = nettingReport.partyCurrency

    override val totalNoSaving: String
        get() = nettingReport.totalNoSaving.toString()
    val savings get() = nettingReport.savingReports
}

class SavingReportImpl(private val savingReport: SavingReport) : ISavingReport {
    override val counterPartyId: String
        get() = savingReport.counterPartyId

    override val preFee: String
        get() = textFormatter.formatFee(savingReport.before.fee)

    override val prePaymentCount: String
        get() = savingReport.before.paymentCount.toString()

    override val preCashTransfer: String
        get() = textFormatter.formatFee(savingReport.before.cashTransfer)

    override val postFee: String
        get() = textFormatter.formatFee(savingReport.after.fee)

    override val postPaymentCount: String
        get() = savingReport.after.paymentCount.toString()
    override val postCashTransfer: String
        get() = textFormatter.formatFee(savingReport.after.cashTransfer)

    override val feeSaving: String
        get() = textFormatter.formatFee(savingReport.feeSaving)

    override val cashTransferSaving: String
        get() = textFormatter.formatFee(savingReport.cashTransferSaving)

    override val noSaving: String
        get() = (savingReport.before.paymentCount - savingReport.after.paymentCount).toString()
}