package com.waiphyo.marketflow.di

import com.waiphyo.marketflow.data.repository.PriceGenerator
import com.waiphyo.marketflow.data.repository.RandomWalkPriceGenerator
import com.waiphyo.marketflow.data.repository.StockRepositoryImpl
import com.waiphyo.marketflow.data.websocket.PriceFeedStream
import com.waiphyo.marketflow.data.websocket.WebSocketManager
import com.waiphyo.marketflow.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        impl: StockRepositoryImpl,
    ): StockRepository

    @Binds
    @Singleton
    abstract fun bindPriceFeedStream(
        impl: WebSocketManager,
    ): PriceFeedStream

    @Binds
    @Singleton
    abstract fun bindPriceGenerator(
        impl: RandomWalkPriceGenerator,
    ): PriceGenerator
}
