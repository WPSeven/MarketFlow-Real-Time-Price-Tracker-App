package com.waiphyo.marketflow.data.repository.fakes

import com.waiphyo.marketflow.data.model.PriceMessage
import com.waiphyo.marketflow.data.websocket.PriceFeedStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePriceFeedStream : PriceFeedStream {
    private val messageFlow = MutableSharedFlow<PriceMessage>(extraBufferCapacity = 64)
    private val connectedFlow = MutableStateFlow(false)

    val sentMessages = mutableListOf<PriceMessage>()

    override val messages: Flow<PriceMessage> = messageFlow
    override val isConnected: StateFlow<Boolean> = connectedFlow

    override fun connect() {
        connectedFlow.value = true
    }

    override fun disconnect() {
        connectedFlow.value = false
    }

    override fun send(message: PriceMessage) {
        sentMessages += message
    }

    fun emitIncoming(message: PriceMessage) {
        messageFlow.tryEmit(message)
    }
}

