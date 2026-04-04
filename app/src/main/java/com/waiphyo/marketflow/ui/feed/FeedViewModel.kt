package com.waiphyo.marketflow.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waiphyo.marketflow.data.model.StockSymbol
import com.waiphyo.marketflow.domain.usecase.ObserveFeedUseCase
import com.waiphyo.marketflow.domain.usecase.ToggleFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class PriceSortOrder {
    DESC,
    ASC,
}

data class FeedUiState(
    val stocks: List<StockSymbol> = emptyList(),
    val isConnected: Boolean = false,
    val isFeedRunning: Boolean = false,
    val sortOrder: PriceSortOrder = PriceSortOrder.DESC,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    observeFeedUseCase: ObserveFeedUseCase,
    private val toggleFeedUseCase: ToggleFeedUseCase,
) : ViewModel() {

    private val isFeedRunning = MutableStateFlow(false)
    private val sortOrder = MutableStateFlow(PriceSortOrder.DESC)
    private val feedObservation = observeFeedUseCase()
        .onEach { observation ->
            if (!observation.isConnected) {
                isFeedRunning.value = false
            }
        }

    val uiState: StateFlow<FeedUiState> = combine(
        feedObservation,
        isFeedRunning,
        sortOrder,
    ) { observation, isRunning, currentSortOrder ->
        val sortedStocks = when (currentSortOrder) {
            PriceSortOrder.DESC -> observation.stocks.sortedByDescending { it.price }
            PriceSortOrder.ASC -> observation.stocks.sortedBy { it.price }
        }
        FeedUiState(
            stocks = sortedStocks,
            isConnected = observation.isConnected,
            isFeedRunning = isRunning,
            sortOrder = currentSortOrder,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState(),
    )

    fun toggleFeed() {
        isFeedRunning.value = toggleFeedUseCase(isFeedRunning.value)
    }

    fun toggleSortOrder() {
        sortOrder.value = when (sortOrder.value) {
            PriceSortOrder.DESC -> PriceSortOrder.ASC
            PriceSortOrder.ASC -> PriceSortOrder.DESC
        }
    }

    override fun onCleared() {
        super.onCleared()
        toggleFeedUseCase.stop()
        isFeedRunning.value = false
    }
}
