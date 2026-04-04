package com.waiphyo.marketflow.data

import app.cash.turbine.test
import com.waiphyo.marketflow.data.model.FlashState
import com.waiphyo.marketflow.data.model.PriceMessage
import com.waiphyo.marketflow.data.model.STOCK_CATALOGUE
import com.waiphyo.marketflow.data.repository.FeedReducer
import com.waiphyo.marketflow.data.repository.FlashResetPolicy
import com.waiphyo.marketflow.data.repository.RandomWalkPriceGenerator
import com.waiphyo.marketflow.data.repository.StockRepositoryImpl
import com.waiphyo.marketflow.data.websocket.PriceFeedStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [StockRepositoryImpl].
 *
 * [PriceFeedStream] is replaced with a hand-rolled fake so no real network
 * connection is opened and message injection stays synchronous.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeFeed: FakePriceFeed
    private lateinit var repository: StockRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeFeed = FakePriceFeed()
        repository = StockRepositoryImpl(
            priceFeedStream = fakeFeed,
            priceGenerator = RandomWalkPriceGenerator(),
            feedReducer = FeedReducer(),
            flashResetPolicy = FlashResetPolicy(),
            defaultDispatcher = testDispatcher,
            appScope = CoroutineScope(testDispatcher),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial stock list contains 25 symbols`() {
        assertEquals(25, repository.stocks.value.size)
    }

    @Test
    fun `startFeed calls priceFeedStream connect`() {
        repository.startFeed()
        assertTrue(fakeFeed.connectCalled)
        repository.stopFeed()
    }

    @Test
    fun `stopFeed calls priceFeedStream disconnect`() {
        repository.startFeed()
        repository.stopFeed()
        assertTrue(fakeFeed.disconnectCalled)
    }

    @Test
    fun `receiving a price update applies new price`() = runTest {
        repository.startFeed()

        repository.stocks.test {
            awaitItem() // initial

            val targetSymbol = STOCK_CATALOGUE.first().symbol
            val newPrice = 999.99
            fakeFeed.emitIncoming(PriceMessage(symbol = targetSymbol, price = newPrice))
            advanceTimeBy(100)

            val updated = awaitItem()
            assertEquals(newPrice, updated.find { it.symbol == targetSymbol }!!.price, 0.001)

            cancelAndIgnoreRemainingEvents()
        }

        repository.stopFeed()
    }

    @Test
    fun `price increase sets FlashState UP`() = runTest {
        repository.startFeed()

        repository.stocks.test {
            awaitItem()

            val targetSymbol = STOCK_CATALOGUE.first().symbol
            val currentPrice = repository.stocks.value.find { it.symbol == targetSymbol }!!.price
            fakeFeed.emitIncoming(PriceMessage(symbol = targetSymbol, price = currentPrice + 10.0))
            advanceTimeBy(100)

            val updated = awaitItem()
            assertEquals(FlashState.UP, updated.find { it.symbol == targetSymbol }!!.flashState)

            cancelAndIgnoreRemainingEvents()
        }

        repository.stopFeed()
    }

    @Test
    fun `price decrease sets FlashState DOWN`() = runTest {
        repository.startFeed()

        repository.stocks.test {
            awaitItem()

            val targetSymbol = STOCK_CATALOGUE.first().symbol
            val currentPrice = repository.stocks.value.find { it.symbol == targetSymbol }!!.price
            fakeFeed.emitIncoming(PriceMessage(symbol = targetSymbol, price = currentPrice - 10.0))
            advanceTimeBy(100)

            val updated = awaitItem()
            assertEquals(FlashState.DOWN, updated.find { it.symbol == targetSymbol }!!.flashState)

            cancelAndIgnoreRemainingEvents()
        }

        repository.stopFeed()
    }

    @Test
    fun `list remains sorted after price update`() = runTest {
        repository.startFeed()

        repository.stocks.test {
            awaitItem()

            val lowestSymbol = repository.stocks.value.last().symbol
            fakeFeed.emitIncoming(PriceMessage(symbol = lowestSymbol, price = Double.MAX_VALUE))
            advanceTimeBy(100)

            val updated = awaitItem()
            val prices = updated.map { it.price }
            assertTrue("List must be sorted descending", prices == prices.sortedDescending())
            assertEquals(lowestSymbol, updated.first().symbol)

            cancelAndIgnoreRemainingEvents()
        }

        repository.stopFeed()
    }
}

// ── Fake PriceFeedStream ──────────────────────────────────────────────────────

private class FakePriceFeed : PriceFeedStream {
    private val _messages = MutableSharedFlow<PriceMessage>(extraBufferCapacity = 64)
    override val messages: Flow<PriceMessage> = _messages

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected

    var connectCalled = false
    var disconnectCalled = false

    override fun connect() {
        connectCalled = true
        _isConnected.value = true
    }

    override fun disconnect() {
        disconnectCalled = true
        _isConnected.value = false
    }

    override fun send(message: PriceMessage) {
        _messages.tryEmit(message)
    }

    suspend fun emitIncoming(message: PriceMessage) {
        _messages.emit(message)
    }
}
