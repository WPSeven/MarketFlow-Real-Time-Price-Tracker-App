package com.waiphyo.marketflow.data.repository

import com.waiphyo.marketflow.data.model.FlashState
import com.waiphyo.marketflow.data.model.PriceMessage
import com.waiphyo.marketflow.data.model.StockSymbol
import javax.inject.Inject

class FeedReducer @Inject constructor() {

    fun applyPriceUpdate(
        current: List<StockSymbol>,
        msg: PriceMessage,
    ): List<StockSymbol> {
        return current.map { stock ->
            if (stock.symbol == msg.symbol) {
                val flash = when {
                    msg.price > stock.price -> FlashState.UP
                    msg.price < stock.price -> FlashState.DOWN
                    else -> FlashState.NONE
                }
                stock.copy(previousPrice = stock.price, price = msg.price, flashState = flash)
            } else {
                stock
            }
        }.sortedByDescending { it.price }
    }

    fun clearFlash(
        current: List<StockSymbol>,
        symbol: String,
    ): List<StockSymbol> {
        return current.map { stock ->
            if (stock.symbol == symbol) stock.copy(flashState = FlashState.NONE) else stock
        }
    }
}

