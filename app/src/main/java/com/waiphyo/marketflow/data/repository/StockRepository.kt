package com.waiphyo.marketflow.data.repository

import com.waiphyo.marketflow.data.model.PriceMessage
import com.waiphyo.marketflow.data.model.STOCK_CATALOGUE
import com.waiphyo.marketflow.data.model.StockSymbol
import com.waiphyo.marketflow.data.websocket.PriceFeedStream
import com.waiphyo.marketflow.di.ApplicationScope
import com.waiphyo.marketflow.di.DefaultDispatcher
import com.waiphyo.marketflow.domain.repository.StockRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TICK_INTERVAL_MS = 2_000L

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val priceFeedStream: PriceFeedStream,
    private val priceGenerator: PriceGenerator,
    private val feedReducer: FeedReducer,
    private val flashResetPolicy: FlashResetPolicy,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : StockRepository {

    private val _stocks = MutableStateFlow(STOCK_CATALOGUE)
    override val stocks: StateFlow<List<StockSymbol>> = _stocks.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var tickerJob: Job? = null
    private var echoJob: Job? = null
    private var connectionJob: Job? = null
    private val flashResetJobs = mutableMapOf<String, Job>()

    override fun startFeed() {
        priceFeedStream.connect()
        startConnectionCollector()
        startEchoCollector()
        startTicker()
    }

    override fun stopFeed() {
        tickerJob?.cancel()
        echoJob?.cancel()
        connectionJob?.cancel()
        flashResetJobs.values.forEach { it.cancel() }
        flashResetJobs.clear()
        tickerJob = null
        echoJob = null
        connectionJob = null
        priceFeedStream.disconnect()
        _isConnected.value = false
    }

    private fun startConnectionCollector() {
        if (connectionJob?.isActive == true) return
        connectionJob = priceFeedStream.isConnected
            .onEach { connected -> _isConnected.value = connected }
            .launchIn(appScope)
    }

    private fun startEchoCollector() {
        if (echoJob?.isActive == true) return
        echoJob = appScope.launch(defaultDispatcher) {
            priceFeedStream.messages.collect { message ->
                applyPriceUpdate(message)
            }
        }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = appScope.launch(defaultDispatcher) {
            while (true) {
                delay(TICK_INTERVAL_MS)
                if (!priceFeedStream.isConnected.value) continue
                _stocks.value.forEach { stock ->
                    val newPrice = priceGenerator.generate(stock.price)
                    priceFeedStream.send(PriceMessage(symbol = stock.symbol, price = newPrice))
                }
            }
        }
    }

    private fun applyPriceUpdate(message: PriceMessage) {
        _stocks.update { current ->
            feedReducer.applyPriceUpdate(current = current, msg = message)
        }

        val updatedStock = _stocks.value.firstOrNull { it.symbol == message.symbol } ?: return
        if (!flashResetPolicy.shouldScheduleReset(updatedStock.flashState)) return

        flashResetJobs[message.symbol]?.cancel()
        flashResetJobs[message.symbol] = appScope.launch(defaultDispatcher) {
            delay(flashResetPolicy.resetDelayMs)
            _stocks.update { current ->
                feedReducer.clearFlash(current = current, symbol = message.symbol)
            }
        }
    }
}
