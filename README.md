# MarketFlow : Real-Time-Price-Tracker-App




Current implementation ( ../data/websocket/WebSocketManager.kt )
1. Request flow (connect)
2. connect() builds an OkHttp Request with the injected WebSocket URL.
3. client.newWebSocket(request, listener) starts the WebSocket handshake (HTTP upgrade to WebSocket).
4. When handshake succeeds, onOpen() is called and isConnected becomes true.
5. Response flow (incoming server frames)
6. Server text frames arrive in onMessage(webSocket, text).
7. The raw JSON string is decoded to PriceMessage using Json.decodeFromString.
8. Decoded objects are pushed into a Channel.
9. Consumers observe them via messages: Flow<PriceMessage> using receiveAsFlow().
10. Outgoing flow (client frames)
11. send(message) encodes PriceMessage to JSON via Json.encodeToString.
12. The JSON text is sent through webSocket?.send(text).
13. Connection state handling1. isConnected is a StateFlow<Boolean>.
14. It is set to false in disconnect(), onClosing(), onClosed(), and onFailure().
15. It is set to true only in onOpen().
16. Why this matches your UI logic1. UI/ViewModel can observe isConnected for Start/Stop state.
17. UI/ViewModel can observe messages as a reactive stream.
18. Repository/use case layer can reduce incoming PriceMessage events into feed state without direct socket access.