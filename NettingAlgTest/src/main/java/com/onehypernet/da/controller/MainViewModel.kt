package com.onehypernet.da.controller

import com.onehypernet.da.core.MutableLiveData
import com.onehypernet.da.core.viewmodel.ViewModel
import com.onehypernet.da.core.viewmodel.launch
import com.onehypernet.da.helper.CSVLoader
import com.onehypernet.da.helper.textFormatter
import com.onehypernet.da.view.*
import com.onehypernet.da.view.Transaction
import com.onehypernet.extension.asBoolean
import com.onehypernet.extension.throws
import com.onehypernet.model.*
import com.onehypernet.netting.exception.MissingException
import com.onehypernet.netting.optimize.ParameterLookup
import com.onehypernet.netting.optimize.v4.OptimizeNettingImpl
import com.onehypernet.netting.report.NettingReporterImpl
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.*

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

    fun optimize(items: List<ITransaction>, bridging: String?) = launch(loading, error, Dispatchers.IO) {
        val bridgingCurrency = kotlin.runCatching {
            Currency.getInstance(bridging)
        }.getOrNull() ?: throws("Can not parse currency $bridging")

        val lookup = createParamLookup() ?: return@launch
        val optimize = OptimizeNettingImpl(lookup = lookup, bridging = bridgingCurrency.currencyCode)
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
        val totalFeeValue = optimize.fxCalculator.getTotalFee(result.payments)
        println("Total fee=${totalFeeValue}, Simulated = $simulatedFeeValue")

        (trans.map { it.fromPartyId } + trans.map { it.toPartyId }).toSet().forEach { party ->
            val before = optimize.fxCalculator.getBalance(
                party,
                trans.filter { it.fromPartyId == party || it.toPartyId == party })
            val after = optimize.fxCalculator.getBalance(
                party,
                result.payments.filter { it.fromPartyId == party || it.toPartyId == party })
            println("Party position $party Before=${before} After=${after}, Deviation=${after - before}")
        }

        instructions.post(result.payments.map { NettingPaymentHolder(it, lookup) })
        totalFee.post("${textFormatter.formatFee(totalFeeValue)}$")
        simulatedFee.post("${textFormatter.formatFee(simulatedFeeValue)}$")
        val reporter = NettingReporterImpl(optimize.lookup)
        val reports = reporter.getPartyReports(result.transactions, result.payments)
        report.post(NettingResult(result, reports, totalFeeValue, simulatedFeeValue))
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

class NettingResult(
    val result: OptimizedResult,
    val reports: List<PartyReport>,
    val fee: Double,
    val simulatedFee: Double
)