package com.waiphyo.marketflow.data.model

import kotlinx.serialization.Serializable

/**
 * JSON payload sent to / echoed back from the WebSocket server.
 *
 * Example: {"symbol":"AAPL","price":183.12}
 */
@Serializable
data class PriceMessage(
    val symbol: String,
    val price: Double,
)
