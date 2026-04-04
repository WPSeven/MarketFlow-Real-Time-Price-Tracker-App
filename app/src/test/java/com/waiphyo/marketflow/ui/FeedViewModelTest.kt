package com.waiphyo.marketflow.ui

import app.cash.turbine.test
import com.waiphyo.marketflow.data.model.STOCK_CATALOGUE
import com.waiphyo.marketflow.domain.usecase.FeedObservation
import com.waiphyo.marketflow.domain.usecase.ObserveFeedUseCase
import com.waiphyo.marketflow.domain.usecase.ToggleFeedUseCase
import com.waiphyo.marketflow.ui.feed.FeedViewModel
import com.waiphyo.marketflow.ui.feed.PriceSortOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var observeFeedUseCase: ObserveFeedUseCase
    private lateinit var toggleFeedUseCase: ToggleFeedUseCase
    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        observeFeedUseCase = mockk<ObserveFeedUseCase>()
        every { observeFeedUseCase() } returns flowOf(FeedObservation(stocks = STOCK_CATALOGUE, isConnected = false))

        toggleFeedUseCase = mockk<ToggleFeedUseCase>(relaxed = true)
        every { toggleFeedUseCase(isRunning = false) } returns true
        every { toggleFeedUseCase(isRunning = true) } returns false

        viewModel = FeedViewModel(observeFeedUseCase, toggleFeedUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has feed not running`() = runTest {
        // backgroundScope keeps uiState active (WhileSubscribed requires a subscriber)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFeedRunning)
    }

    @Test
    fun `initial state exposes stocks from use case`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(STOCK_CATALOGUE.size, viewModel.uiState.value.stocks.size)
    }

    @Test
    fun `toggleFeed starts feed and sets isFeedRunning true`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFeed()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFeedRunning)
        verify { toggleFeedUseCase(isRunning = false) }
    }

    @Test
    fun `toggleFeed twice stops feed and sets isFeedRunning false`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFeed()  // start
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleFeed()  // stop
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFeedRunning)
        verify { toggleFeedUseCase(isRunning = true) }
    }

    @Test
    fun `default sort order is DESC`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PriceSortOrder.DESC, viewModel.uiState.value.sortOrder)
    }

    @Test
    fun `toggleSortOrder switches from DESC to ASC`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSortOrder()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PriceSortOrder.ASC, viewModel.uiState.value.sortOrder)
    }

    @Test
    fun `toggleSortOrder twice returns to DESC`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSortOrder()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleSortOrder()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PriceSortOrder.DESC, viewModel.uiState.value.sortOrder)
    }
}
