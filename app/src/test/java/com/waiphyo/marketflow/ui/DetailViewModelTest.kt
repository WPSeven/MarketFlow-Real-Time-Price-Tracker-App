package com.waiphyo.marketflow.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.waiphyo.marketflow.data.model.STOCK_CATALOGUE
import com.waiphyo.marketflow.domain.repository.StockRepository
import com.waiphyo.marketflow.ui.detail.DetailViewModel
import com.waiphyo.marketflow.ui.navigation.NavArgs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val targetSymbol = STOCK_CATALOGUE.first().symbol

    private lateinit var mockRepository: StockRepository
    private lateinit var viewModel: DetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepository = mockk(relaxed = true) {
            every { stocks } returns MutableStateFlow(STOCK_CATALOGUE)
            every { isConnected } returns MutableStateFlow(false)
        }

        val savedState = SavedStateHandle(mapOf(NavArgs.SYMBOL to targetSymbol))
        viewModel = DetailViewModel(savedState, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits stock matching saved symbol`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.stock)
            assertEquals(targetSymbol, state.stock!!.symbol)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState updates when repository emits new price`() = runTest {
        val stocksFlow = MutableStateFlow(STOCK_CATALOGUE)
        every { mockRepository.stocks } returns stocksFlow

        val savedState = SavedStateHandle(mapOf(NavArgs.SYMBOL to targetSymbol))
        val vm = DetailViewModel(savedState, mockRepository)

        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            awaitItem() // initial

            val updatedList = STOCK_CATALOGUE.map {
                if (it.symbol == targetSymbol) it.copy(price = 9999.99) else it
            }
            stocksFlow.value = updatedList
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(9999.99, updated.stock!!.price, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
