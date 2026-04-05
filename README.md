# MarketFlow — Real-Time Price Tracker

A real-time stock price tracker for Android built with Jetpack Compose, WebSocket streaming, and Clean Architecture.

---

## Screenshots

| Feed (Light) | Feed (Dark) | Detail | Account |
|:---:|:---:|:---:|:---:|
| ![Feed Light](https://github.com/user-attachments/assets/931692f9-d68f-41d7-ab80-457846a96731) | ![Feed Dark](https://github.com/user-attachments/assets/70dcb4a0-a22a-467c-ab31-c71bc92c4a8c) | ![Detail](https://github.com/user-attachments/assets/3007e903-5003-49ba-a89f-222b1a46487f) | ![Account](https://github.com/user-attachments/assets/f3e7734b-cdaa-4cf2-843e-4ae8d7ed2235) |

## Demo

https://github.com/user-attachments/assets/887d8465-8603-4869-9e4f-466b7affb4bd

https://github.com/user-attachments/assets/f8fe1b14-cb18-467b-b938-5e0baa371593

---

## Features

- **Live price feed** — 25 stock symbols updated every 2 seconds via WebSocket echo
- **Flash animation** — row flashes green (↑) or red (↓) for 300 ms on each price change
- **Sort toggle** — switch between highest-price-first and lowest-price-first instantly
- **Start / Stop feed** — connect or disconnect the WebSocket stream on demand
- **Connection indicator** — green dot (connected) / red dot (disconnected) in the top bar
- **Detail screen** — full symbol info with animated price card and flash color
- **Deep link** — `stocks://symbol/{symbol}` opens the detail screen directly
- **Light & dark theme** — switchable from the Account screen, persists for the session
- **Bottom navigation** — Feed, Favorites, Account tabs

---

## Architecture

Clean Architecture with MVVM, single-module.

```
ui/          Jetpack Compose screens + ViewModels
domain/      Pure Kotlin use cases and repository interface
data/        Repository impl, WebSocket, price generation
di/          Hilt modules and qualifiers
```

**Data flow**

```
PriceGenerator
      │  (every 2s per symbol)
      ▼
WebSocketManager ──send──► wss://ws.postman-echo.com/raw
                  ◄─echo──
      │
      ▼  PriceMessage (Channel)
StockRepositoryImpl
      │  FeedReducer applies update + FlashResetPolicy schedules 300ms reset
      ▼  StateFlow<List<StockSymbol>>
ObserveFeedUseCase  (combines stocks + connection state)
      │
      ▼
FeedViewModel / DetailViewModel
      │  StateFlow<UiState>
      ▼
Compose UI
```

---

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.8.2 |
| DI | Hilt 2.59.2 |
| WebSocket | OkHttp 4.12.0 |
| HTTP | Retrofit 2.11.0 |
| Serialization | Kotlinx Serialization 1.7.3 |
| Async | Kotlin Coroutines + Flow 1.9.0 |
| Testing | JUnit 4, MockK, Turbine |

**SDK:** Min 24 · Target/Compile 36 · Java 11 · Kotlin 2.2.10

---

## Project Structure

```
app/src/main/java/com/waiphyo/marketflow/
├── data/
│   ├── model/          StockSymbol.kt, PriceMessage.kt
│   ├── repository/     StockRepositoryImpl, FeedReducer, FlashResetPolicy, PriceGenerator
│   └── websocket/      WebSocketManager, PriceFeedStream
├── di/                 AppModule, RepositoryModule, Qualifiers
├── domain/
│   ├── repository/     StockRepository (interface)
│   └── usecase/        ObserveFeedUseCase, ToggleFeedUseCase
└── ui/
    ├── feed/           FeedScreen, FeedViewModel
    ├── detail/         DetailScreen, DetailViewModel
    ├── favorites/      FavoritesScreen
    ├── account/        AccountScreen
    ├── navigation/     AppNavigation
    └── theme/          Color, Theme, Type
```

---

## Build & Run

```bash
# Clone
git clone https://github.com/<your-username>/MarketFlow.git
cd MarketFlow

# Debug build
./gradlew assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# Lint + all checks
./gradlew check
```

Open in Android Studio Meerkat or later and run on a device/emulator with API 24+.

---

## Deep Link

```
adb shell am start \
  -W -a android.intent.action.VIEW \
  -d "stocks://symbol/AAPL" \
  com.waiphyo.marketflow
```

---

## WebSocket Integration

- **Server:** `wss://ws.postman-echo.com/raw` (echo server)
- **Protocol:** App sends a JSON `PriceMessage` for each symbol every 2 seconds; the server echoes it back; the repository applies the update
- **Connection state:** `isConnected: StateFlow<Boolean>` drives the UI indicator and is shared across both screens without duplicate connections

---

## Testing

```
test/
├── data/StockRepositoryImplTest   Flash lifecycle, ticker emission, connection behavior
└── ui/
    ├── FeedViewModelTest          ViewModel state with FakeStockRepository
    └── DetailViewModelTest        Symbol resolution via SavedStateHandle
```

Key test utilities:
- `FakePriceFeedStream` — injects WebSocket messages via `emitIncoming()`
- Turbine — asserts Flow emissions
- `StandardTestDispatcher` + `advanceTimeBy` — controls 300 ms flash reset timing
