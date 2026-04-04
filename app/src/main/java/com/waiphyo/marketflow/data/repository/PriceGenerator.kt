package com.waiphyo.marketflow.data.repository

import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

interface PriceGenerator {
    fun generate(current: Double): Double
}

class RandomWalkPriceGenerator @Inject constructor() : PriceGenerator {
    override fun generate(current: Double): Double {
        val changePct = (Random.nextDouble() - 0.5) * 0.02
        val newPrice = current * (1.0 + changePct)
        return String.format(Locale.US, "%.2f", newPrice).toDouble()
    }
}

