package com.waiphyo.marketflow.data.model

/**
 * Represents a single stock symbol with its live price data.
 *
 * @param symbol   Ticker symbol (e.g. "AAPL").
 * @param name     Human-readable company name.
 * @param price    Current price in USD.
 * @param previousPrice  Price before the last update (used to compute direction).
 * @param description  Brief company description shown on the details screen.
 * @param flashState   Transient flash colour emitted when a new price arrives.
 */
data class StockSymbol(
    val symbol: String,
    val name: String,
    val price: Double,
    val previousPrice: Double,
    val description: String,
    val flashState: FlashState = FlashState.NONE,
) {
    val priceChange: Double get() = price - previousPrice
    val isUp: Boolean get() = priceChange >= 0
    val changeIndicator: String get() = if (isUp) "↑" else "↓"
}

enum class FlashState { NONE, UP, DOWN }

/** Catalogue of the 25 symbols used by the app, with seed prices. */
val STOCK_CATALOGUE: List<StockSymbol> = listOf(
    StockSymbol("AAPL",  "Apple Inc.",               182.63, 182.63, "Apple designs iPhones, Macs, and the world's largest app marketplace."),
    StockSymbol("GOOG",  "Alphabet Inc.",             175.41, 175.41, "Alphabet operates Google Search, YouTube, and Google Cloud."),
    StockSymbol("TSLA",  "Tesla, Inc.",               248.42, 248.42, "Tesla designs electric vehicles, energy storage, and solar products."),
    StockSymbol("AMZN",  "Amazon.com, Inc.",          196.85, 196.85, "Amazon is the world's largest e-commerce and cloud-computing company."),
    StockSymbol("MSFT",  "Microsoft Corporation",     415.23, 415.23, "Microsoft builds Windows, Azure cloud, and the Office productivity suite."),
    StockSymbol("NVDA",  "NVIDIA Corporation",        875.45, 875.45, "NVIDIA leads the GPU market and powers modern AI data centres."),
    StockSymbol("META",  "Meta Platforms, Inc.",      512.76, 512.76, "Meta owns Facebook, Instagram, and WhatsApp, reaching 3 billion users."),
    StockSymbol("BRK.B", "Berkshire Hathaway",        385.90, 385.90, "Warren Buffett's conglomerate spanning insurance, railroads, and energy."),
    StockSymbol("JPM",   "JPMorgan Chase & Co.",      204.31, 204.31, "America's largest bank by assets, offering consumer and investment banking."),
    StockSymbol("V",     "Visa Inc.",                 279.55, 279.55, "Visa operates the world's largest payment network across 200+ countries."),
    StockSymbol("UNH",   "UnitedHealth Group",        521.18, 521.18, "UnitedHealth provides health benefits and data analytics services."),
    StockSymbol("XOM",   "Exxon Mobil Corporation",   115.67, 115.67, "ExxonMobil is a leading integrated oil and gas supermajor."),
    StockSymbol("LLY",   "Eli Lilly and Company",     802.34, 802.34, "Eli Lilly produces breakthrough diabetes and obesity medications."),
    StockSymbol("JNJ",   "Johnson & Johnson",         158.92, 158.92, "J&J manufactures pharmaceuticals, medical devices, and consumer health."),
    StockSymbol("AVGO",  "Broadcom Inc.",             1312.50, 1312.50, "Broadcom designs semiconductors and infrastructure software solutions."),
    StockSymbol("PG",    "Procter & Gamble",          163.44, 163.44, "P&G sells household brands including Tide, Pampers, and Gillette."),
    StockSymbol("MA",    "Mastercard Incorporated",   474.88, 474.88, "Mastercard connects consumers and merchants through secure payment tech."),
    StockSymbol("HD",    "The Home Depot, Inc.",      363.20, 363.20, "Home Depot is the world's largest home-improvement retailer."),
    StockSymbol("ABBV",  "AbbVie Inc.",               167.55, 167.55, "AbbVie develops immunology drugs including blockbuster Humira."),
    StockSymbol("CVX",   "Chevron Corporation",       152.38, 152.38, "Chevron is a diversified energy company active in 180+ countries."),
    StockSymbol("MRK",   "Merck & Co., Inc.",         127.19, 127.19, "Merck's portfolio includes Keytruda, the top-selling cancer drug."),
    StockSymbol("COST",  "Costco Wholesale",          893.25, 893.25, "Costco runs a global chain of membership-only warehouse clubs."),
    StockSymbol("AMD",   "Advanced Micro Devices",    167.88, 167.88, "AMD makes CPUs and GPUs competing directly with Intel and NVIDIA."),
    StockSymbol("NFLX",  "Netflix, Inc.",             687.42, 687.42, "Netflix is the world's leading streaming service with 270 M+ members."),
    StockSymbol("ORCL",  "Oracle Corporation",        134.76, 134.76, "Oracle provides enterprise databases, ERP software, and cloud services."),
)
