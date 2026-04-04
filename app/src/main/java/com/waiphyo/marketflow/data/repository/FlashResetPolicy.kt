package com.waiphyo.marketflow.data.repository

import com.waiphyo.marketflow.data.model.FlashState
import javax.inject.Inject

private const val DEFAULT_FLASH_RESET_MS = 1_000L

class FlashResetPolicy @Inject constructor() {
    val resetDelayMs: Long = DEFAULT_FLASH_RESET_MS

    fun shouldScheduleReset(flashState: FlashState): Boolean {
        return flashState != FlashState.NONE
    }
}

