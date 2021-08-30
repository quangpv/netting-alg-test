package com.onehypernet.da.controller

import com.onehypernet.da.core.Controller
import com.onehypernet.da.core.StageLoader
import com.onehypernet.da.core.viewmodel.viewModel
import com.onehypernet.da.extensions.safe
import com.onehypernet.da.functional.OpenGraphAction
import com.onehypernet.da.view.*
import com.onehypernet.da.widget.ErrorDialog
import com.onehypernet.da.widget.ExportCSVDialog
import com.onehypernet.da.widget.ImportCSVAction
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TextField


class MainController : Controller() {
    lateinit var tbTransaction: TableView<ITransaction>
    lateinit var tbNettingParam: TableView<INettingParams>
    lateinit var tbLocation: TableView<Location>
    lateinit var tbNettingResult: TableView<NettingPaymentHolder>

    lateinit var btnAddTransaction: Button
    lateinit var btnRemoveTransaction: Button

    lateinit var btnOptimize: Button
    lateinit var btnViewResult: Button
    lateinit var btnViewResultGraph: Button
    lateinit var btnViewTransactionGraph: Button

    lateinit var btnImportCSV: Button
    lateinit var btnExportCSV: Button
    lateinit var btnExportPaymentCSV: Button

    lateinit var btnImportParams: Button
    lateinit var btnImportLocation: Button

    lateinit var lbSimulatedFee: Label
    lateinit var lbTotalFee: Label
    lateinit var lbSolvingTime: Label
    lateinit var lbVersion: Label

    lateinit var edtBridging: TextField

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(node: Node) {
        super.onCreate(node)
        val transactionAdapter = TransactionAdapter(tbTransaction)
        val nettingParamAdapter = NettingParamAdapter(tbNettingParam)
        val locationAdapter = LocationAdapter(tbLocation)
        val nettingResultAdapter = NettingPaymentAdapter(tbNettingResult)

        lbVersion.text = "v0.0.4"
        btnAddTransaction.setOnMouseClicked {
            transactionAdapter.addEmpty()
        }
        btnRemoveTransaction.setOnMouseClicked {
            transactionAdapter.removeSelected()
        }
        btnOptimize.setOnMouseClicked {
            viewModel.optimize(transactionAdapter.items, edtBridging.text)
        }
        btnImportCSV.onMouseClicked = ImportCSVAction(node, "Transactions") { viewModel.load(it) }

        btnImportParams.onMouseClicked = ImportCSVAction(node, "Netting Params") { viewModel.loadParams(it) }
        btnImportLocation.onMouseClicked = ImportCSVAction(node, "Locations") { viewModel.loadLocations(it) }

        btnViewTransactionGraph.onMouseClicked = OpenGraphAction("Transactions") {
            transactionAdapter.items
        }

        btnViewResultGraph.onMouseClicked = OpenGraphAction("Payment Results") {
            viewModel.instructions.value.orEmpty()
        }
        btnExportCSV.onMouseClicked = ExportCSVDialog.Action(node) { transactionAdapter.items }

        btnExportPaymentCSV.onMouseClicked = ExportCSVDialog.Action(node) { nettingResultAdapter.items }

        viewModel.report.observe(this) { result ->
            btnViewResult.setOnMouseClicked {
                StageLoader("result").start(args = result)
            }
            lbSolvingTime.text = "Solving time: ${result?.result?.timeToSolve ?: 0.0} ms"
        }

        viewModel.transactions.observe(this) {
            transactionAdapter.submit(it)
        }

        viewModel.locations.observe(this) {
            locationAdapter.submit(it)
        }

        viewModel.params.observe(this) {
            nettingParamAdapter.submit(it)
        }

        viewModel.error.observe(this) {
            ErrorDialog().show(it)
        }

        viewModel.instructions.observe(this) {
            nettingResultAdapter.submit(it)
        }

        viewModel.simulatedFee.observe(this) {
            lbSimulatedFee.text = it
        }

        viewModel.totalFee.observe(this) {
            lbTotalFee.text = it
        }

        viewModel.loading.observe(this) { loading ->
            arrayOf(
                btnAddTransaction,
                btnRemoveTransaction,
                btnImportCSV,
                btnImportLocation,
                btnImportParams,
                btnOptimize,
                btnViewResultGraph,
                btnViewTransactionGraph,
                btnExportCSV,
                btnViewResult,
            ).forEach {
                it.isDisable = loading.safe(false)
            }
        }

        viewModel.missing.observe(this) {
            ErrorDialog().show(it.safe())
        }

        edtBridging.text = "USD"
    }
}

interface IGraphItem {
    val fromPartyId: String
    val toPartyId: String
    val amount: String
    val currency: String
}