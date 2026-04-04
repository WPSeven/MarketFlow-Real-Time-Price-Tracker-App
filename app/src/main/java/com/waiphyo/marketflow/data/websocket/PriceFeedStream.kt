package com.waiphyo.marketflow.data.websocket

import com.waiphyo.marketflow.data.model.PriceMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PriceFeedStream {
    val messages: Flow<PriceMessage>
    val isConnected: StateFlow<Boolean>

    fun connect()
    fun disconnect()
    fun send(message: PriceMessage)
}

