package com.waiphyo.marketflow.data.repository

import com.waiphyo.marketflow.data.model.FlashState
import com.waiphyo.marketflow.data.model.PriceMessage
import com.waiphyo.marketflow.data.repository.fakes.FakePriceFeedStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FixedPriceGenerator(
    private val generatedPrice: Double,
) : PriceGenerator {
    override fun generate(current: Double): Double = generatedPrice
}

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryImplTest {

    @Test
    fun `start and stop feed update connection state`() = runTest {
        val fakeStream = FakePriceFeedStream()
        val repository = StockRepositoryImpl(
            priceFeedStream = fakeStream,
            priceGenerator = FixedPriceGenerator(generatedPrice = 111.11),
            feedReducer = FeedReducer(),
            flashResetPolicy = FlashResetPolicy(),
            defaultDispatcher = StandardTestDispatcher(testScheduler),
            appScope = backgroundScope,
        )

        repository.startFeed()
        runCurrent()
        assertTrue(repository.isConnected.value)

        repository.stopFeed()
        runCurrent()
        assertFalse(repository.isConnected.value)
    }

    @Test
    fun `incoming message sets flash then resets after policy delay`() = runTest {
        val fakeStream = FakePriceFeedStream()
        val policy = FlashResetPolicy()
        val repository = StockRepositoryImpl(
            priceFeedStream = fakeStream,
            priceGenerator = FixedPriceGenerator(generatedPrice = 111.11),
            feedReducer = FeedReducer(),
            flashResetPolicy = policy,
            defaultDispatcher = StandardTestDispatcher(testScheduler),
            appScope = backgroundScope,
        )

        repository.startFeed()
        runCurrent()

        val target = repository.stocks.value.first()
        fakeStream.emitIncoming(PriceMessage(symbol = target.symbol, price = target.price + 5.0))
        runCurrent()

        val updated = repository.stocks.value.first { it.symbol == target.symbol }
        assertTrue(updated.flashState == FlashState.UP)

        advanceTimeBy(policy.resetDelayMs)
        runCurrent()

        val reset = repository.stocks.value.first { it.symbol == target.symbol }
        assertTrue(reset.flashState == FlashState.NONE)
    }

    @Test
    fun `ticker emits websocket messages while connected`() = runTest {
        val fakeStream = FakePriceFeedStream()
        val repository = StockRepositoryImpl(
            priceFeedStream = fakeStream,
            priceGenerator = FixedPriceGenerator(generatedPrice = 222.22),
            feedReducer = FeedReducer(),
            flashResetPolicy = FlashResetPolicy(),
            defaultDispatcher = StandardTestDispatcher(testScheduler),
            appScope = backgroundScope,
        )

        repository.startFeed()
        runCurrent()

        advanceTimeBy(2_000L)
        runCurrent()

        assertTrue(fakeStream.sentMessages.isNotEmpty())
    }
}

