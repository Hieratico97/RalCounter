package com.monsoonmage.ralcounter.model

data class FlipResult(
    val isWin: Boolean,
    val spellsAtTime: Int,
    val turnNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
