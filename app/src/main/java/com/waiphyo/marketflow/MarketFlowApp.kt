package com.waiphyo.marketflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.  Annotated with [HiltAndroidApp] to trigger Hilt's
 * code generation and create the application-level component.
 */
@HiltAndroidApp
class MarketFlowApp : Application()
