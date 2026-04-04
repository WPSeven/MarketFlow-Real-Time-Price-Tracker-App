package com.waiphyo.marketflow.domain.repository

import com.waiphyo.marketflow.data.model.StockSymbol
import kotlinx.coroutines.flow.StateFlow

interface StockRepository {
    val stocks: StateFlow<List<StockSymbol>>
    val isConnected: StateFlow<Boolean>

    fun startFeed()
    fun stopFeed()
}

