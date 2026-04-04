package com.waiphyo.marketflow.domain.usecase

import com.waiphyo.marketflow.data.model.StockSymbol
import com.waiphyo.marketflow.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class FeedObservation(
    val stocks: List<StockSymbol>,
    val isConnected: Boolean,
)

class ObserveFeedUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    operator fun invoke(): Flow<FeedObservation> {
        return combine(
            stockRepository.stocks,
            stockRepository.isConnected,
        ) { stocks, isConnected ->
            FeedObservation(stocks = stocks, isConnected = isConnected)
        }
    }
}

