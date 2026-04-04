package com.waiphyo.marketflow.data.websocket

import android.util.Log
import com.waiphyo.marketflow.data.model.PriceMessage
import com.waiphyo.marketflow.di.WebSocketClient
import com.waiphyo.marketflow.di.WebSocketUrl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WebSocketManager"

/**
 * Manages a single OkHttp WebSocket connection for the lifetime of the feed.
 *
 * Exposes:
 *  - [messages]   — cold Flow of decoded [PriceMessage] objects received from the server.
 *  - [isConnected] — true once the handshake succeeds.
 *
 * Call [connect] to open, [disconnect] to close, and [send] to write a frame.
 */
@Singleton
class WebSocketManager @Inject constructor(
    @param:WebSocketClient private val client: OkHttpClient,
    private val json: Json,
    @param:WebSocketUrl private val webSocketUrl: String,
) : PriceFeedStream {
    // Channel that delivers decoded messages to collectors.
    private val _messageChannel = Channel<PriceMessage>(capacity = Channel.UNLIMITED)
    override val messages: Flow<PriceMessage> = _messageChannel.receiveAsFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var webSocket: WebSocket? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun connect() {
        if (webSocket != null) return
        Log.d(TAG, "Connecting to $webSocketUrl")
        val request = Request.Builder().url(webSocketUrl).build()
        webSocket = client.newWebSocket(request, listener)
    }

    override fun disconnect() {
        webSocket?.close(1000, "User stopped feed")
        webSocket = null
        _isConnected.value = false
        Log.d(TAG, "Disconnected")
    }

    /** Send a serialised [PriceMessage] frame. */
    override fun send(message: PriceMessage) {
        val text = json.encodeToString(PriceMessage.serializer(), message)
        webSocket?.send(text)
    }

    // ── WebSocketListener ─────────────────────────────────────────────────────

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "onOpen: ${response.code}")
            _isConnected.value = true
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.v(TAG, "onMessage: $text")
            runCatching { json.decodeFromString<PriceMessage>(text) }
                .onSuccess { _messageChannel.trySend(it) }
                .onFailure { Log.w(TAG, "Parse error: ${it.message}") }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosing: $code $reason")
            _isConnected.value = false
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosed: $code $reason")
            _isConnected.value = false
            this@WebSocketManager.webSocket = null
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "onFailure: ${t.message}", t)
            _isConnected.value = false
            this@WebSocketManager.webSocket = null
        }
    }
}
