package com.waiphyo.marketflow.domain.usecase

import com.waiphyo.marketflow.domain.repository.StockRepository
import javax.inject.Inject

class ToggleFeedUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    operator fun invoke(isRunning: Boolean): Boolean {
        return if (isRunning) {
            stockRepository.stopFeed()
            false
        } else {
            stockRepository.startFeed()
            true
        }
    }

    fun stop() {
        stockRepository.stopFeed()
    }
}

