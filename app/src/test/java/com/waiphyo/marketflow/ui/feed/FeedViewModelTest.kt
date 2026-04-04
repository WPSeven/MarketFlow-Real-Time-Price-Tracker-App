package com.waiphyo.marketflow.ui.feed

import com.waiphyo.marketflow.data.model.StockSymbol
import com.waiphyo.marketflow.domain.repository.StockRepository
import com.waiphyo.marketflow.domain.usecase.ObserveFeedUseCase
import com.waiphyo.marketflow.domain.usecase.ToggleFeedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeStockRepository : StockRepository {
    override val stocks = MutableStateFlow<List<StockSymbol>>(emptyList())
    override val isConnected = MutableStateFlow(false)

    override fun startFeed() {
        isConnected.value = true
    }

    override fun stopFeed() {
        isConnected.value = false
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `disconnect resets running state back to false`() = runTest(dispatcher) {
        val repository = FakeStockRepository()
        val viewModel = FeedViewModel(
            observeFeedUseCase = ObserveFeedUseCase(repository),
            toggleFeedUseCase = ToggleFeedUseCase(repository),
        )

        val collectorJob = launch { viewModel.uiState.collect() }

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isFeedRunning)

        viewModel.toggleFeed()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isFeedRunning)

        repository.stopFeed()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isFeedRunning)

        collectorJob.cancel()
    }
}
