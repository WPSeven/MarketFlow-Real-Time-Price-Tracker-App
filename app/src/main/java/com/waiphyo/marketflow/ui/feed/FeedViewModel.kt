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

data class FeedUiState(
    val stocks: List<StockSymbol> = emptyList(),
    val isConnected: Boolean = false,
    val isFeedRunning: Boolean = false,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    observeFeedUseCase: ObserveFeedUseCase,
    private val toggleFeedUseCase: ToggleFeedUseCase,
) : ViewModel() {

    private val isFeedRunning = MutableStateFlow(false)
    private val feedObservation = observeFeedUseCase()
        .onEach { observation ->
            if (!observation.isConnected) {
                isFeedRunning.value = false
            }
        }

    val uiState: StateFlow<FeedUiState> = combine(
        feedObservation,
        isFeedRunning,
    ) { observation, isRunning ->
        FeedUiState(
            stocks = observation.stocks,
            isConnected = observation.isConnected,
            isFeedRunning = isRunning,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState(),
    )

    fun toggleFeed() {
        isFeedRunning.value = toggleFeedUseCase(isFeedRunning.value)
    }

    override fun onCleared() {
        super.onCleared()
        toggleFeedUseCase.stop()
        isFeedRunning.value = false
    }
}
