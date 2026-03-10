package com.monsoonmage.ralcounter.model

data class ManaPool(
    val blue: Int = 0,
    val red: Int = 0,
    val generic: Int = 0
) {
    val total: Int get() = blue + red + generic
}
