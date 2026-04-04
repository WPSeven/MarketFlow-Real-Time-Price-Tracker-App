package com.waiphyo.marketflow.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waiphyo.marketflow.data.model.StockSymbol
import com.waiphyo.marketflow.domain.repository.StockRepository
import com.waiphyo.marketflow.ui.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

data class DetailUiState(
    val stock: StockSymbol? = null,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * Drives the Symbol Details screen.
 *
 * The selected symbol is retrieved from [SavedStateHandle] (populated by
 * Navigation Compose from the route argument), so it survives process death.
 *
 * Because [StockRepository] is a singleton, this ViewModel **shares the same
 * live data stream** as [FeedViewModel] without opening a second WebSocket.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: StockRepository,
) : ViewModel() {

    private val symbol: String = checkNotNull(savedStateHandle[NavArgs.SYMBOL])

    val uiState: StateFlow<DetailUiState> = repository.stocks
        .map { list -> DetailUiState(stock = list.find { it.symbol == symbol }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState(),
        )
}
