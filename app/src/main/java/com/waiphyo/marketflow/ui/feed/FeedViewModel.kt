package com.waiphyo.marketflow.ui.feed

import androidx.lifecycle.ViewModel
import com.example.pricetracker.data.model.STOCK_CATALOGUE
import com.example.pricetracker.data.model.StockSymbol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


data class FeedUiState(
    val stocks: List<StockSymbol> = emptyList(),
    val isConnected: Boolean = false,
    val isFeedRunning: Boolean = false,
)


@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        FeedUiState(
            stocks = STOCK_CATALOGUE.take(8),
            isConnected = true,
            isFeedRunning = true,
        )
    )

    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
}
