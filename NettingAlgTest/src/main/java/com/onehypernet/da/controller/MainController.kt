package com.onehypernet.da.controller

import com.onehypernet.da.core.Controller
import com.onehypernet.da.core.MutableLiveData
import com.onehypernet.da.core.StageLoader
import com.onehypernet.da.core.viewmodel.ViewModel
import com.onehypernet.da.core.viewmodel.launch
import com.onehypernet.da.core.viewmodel.viewModel
import com.onehypernet.da.extensions.safe
import com.onehypernet.da.helper.CSVLoader
import com.onehypernet.da.helper.textFormatter
import com.onehypernet.da.widget.ErrorDialog
import com.onehypernet.da.widget.ImportCSVAction
import com.onehypernet.extension.asBoolean
import com.onehypernet.model.FeeParam
import com.onehypernet.model.NettingResult
import com.onehypernet.model.NettingTransaction
import com.onehypernet.model.PartyLocation
import com.onehypernet.netting.exception.MissingException
import com.onehypernet.netting.optimize.ParameterLookup
import com.onehypernet.netting.optimize.v4.OptimizeNettingImpl
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableView
import kotlinx.coroutines.Dispatchers
import java.io.File


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

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(node: Node) {
        super.onCreate(node)
        val transactionAdapter = TransactionAdapter(tbTransaction)
        val nettingParamAdapter = NettingParamAdapter(tbNettingParam)
        val locationAdapter = LocationAdapter(tbLocation)
        val nettingResultAdapter = NettingPaymentAdapter(tbNettingResult)

        btnAddTransaction.setOnMouseClicked {
            transactionAdapter.addEmpty()
        }
        btnRemoveTransaction.setOnMouseClicked {
            transactionAdapter.removeSelected()
        }
        btnOptimize.setOnMouseClicked {
            viewModel.optimize(transactionAdapter.items)
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
            lbSolvingTime.text = "Solving time: ${result?.timeToSolve ?: 0.0} ms"
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
    }
}

class MainViewModel : ViewModel() {
    val simulatedFee = MutableLiveData<String>()
    val totalFee = MutableLiveData<String>()

    val locations = MutableLiveData<List<Location>>()
    val transactions = MutableLiveData<List<ITransaction>>()
    val params = MutableLiveData<List<INettingParams>>()
    val error = MutableLiveData<Throwable>()
    val instructions = MutableLiveData<List<NettingPaymentHolder>>()
    val report = MutableLiveData<NettingResult>()
    val loading = MutableLiveData<Boolean>()
    val missing = MutableLiveData<String>()

    init {
        simulatedFee.post("0.0")
        totalFee.post("0.0")
    }

    fun load(load: File) = launch(error = error) {
        transactions.post(CSVLoader.load<Transaction>(load))
    }

    fun loadParams(it: File) = launch(error = error) {
        params.post(CSVLoader.load<NettingParams>(it).let {
            if (it.first().fx1 == "From") (it as MutableList).removeAt(0)
            it
        })
    }

    fun optimize(items: List<ITransaction>) = launch(loading, error, Dispatchers.IO) {
        val lookup = createParamLookup() ?: return@launch
        val optimize = OptimizeNettingImpl(lookup = lookup)
        val trans = items.map {
            NettingTransaction(
                it.fromPartyId,
                it.toPartyId,
                it.amount.toDouble(),
                it.currency,
                it.convertible.asBoolean()
            )
        }
        val result = try {
            optimize.process(trans)
        } catch (e: MissingException) {
            missing.post(e.message)
            return@launch
        }
        val simulatedFeeValue = optimize.fxCalculator.getTotalFee(trans)
        println("Total fee=${result.totalFee}, Simulated = $simulatedFeeValue")

        instructions.post(result.payments.map { NettingPaymentHolder(it, lookup) })
        totalFee.post("${textFormatter.formatFee(result.totalFee)}$")
        simulatedFee.post("${textFormatter.formatFee(simulatedFeeValue)}$")
        report.post(result)
    }

    private fun createParamLookup(): ParameterLookup? {
        if (params.value == null || locations.value == null) return null

        val params = params.value?.filter { it.isNotBlank() }?.map {
            FeeParam(
                it.fx1,
                it.fx2,
                it.margin.toDouble(),
                it.feePercent.toDouble(),
                it.feeMin.toDouble(),
                it.feeMax.toDouble(),
                it.fixedFee.toDouble(),
                it.exchangeRate.toDouble(),
                it.location,
                it.toLocations,
            )
        }.orEmpty()
        val locations = locations.value?.map {
            PartyLocation(
                it.partyId, it.locationCode,
                it.currencyCode,
                it.selfConvert.asBoolean()
            )
        }.orEmpty()
        return ParameterLookup(
            params,
            locations
        )
    }

    fun loadLocations(it: File) = launch(error = error) {
        locations.post(CSVLoader.load(it) {
            Location(
                it[0], it[1],
                if (it.size <= 2 && it[2].isBlank()) "" else it[2],
                if (it.size <= 3) "Y" else textFormatter.formatBoolean(it[3])
            )
        })
    }
}

interface IGraphItem {
    val fromPartyId: String
    val toPartyId: String
    val amount: String
    val currency: String
}