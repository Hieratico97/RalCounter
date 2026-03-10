package com.monsoonmage.ralcounter.engine

import kotlin.random.Random

object CoinFlipEngine {
    /** Returns true = WIN (cara), false = LOSE (cruz) */
    fun flip(): Boolean = Random.nextBoolean()
}
