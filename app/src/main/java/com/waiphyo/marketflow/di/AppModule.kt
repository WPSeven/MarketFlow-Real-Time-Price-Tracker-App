package com.waiphyo.marketflow.di

// No explicit @Provides bindings needed here.
//
// Both WebSocketManager and StockRepository carry:
//   @Singleton + @Inject constructor(...)
//
// Hilt resolves and scopes them automatically.  Both the Feed and the Detail
// ViewModels inject the same StockRepository singleton, ensuring a single
// WebSocket connection is shared across the whole app.
//
// This file is kept as a placeholder so the di/ package stays visible and can
// be extended later (e.g. for test fakes, OkHttp interceptors, etc.).
