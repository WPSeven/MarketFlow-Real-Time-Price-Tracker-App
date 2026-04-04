package com.waiphyo.marketflow.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebSocketClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebSocketUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

