package com.nathan.pokemonpricetracker.data.model

/** Prices from one source for one version of a card, e.g. "TCGplayer · Holofoil". */
data class PriceSection(
    val title: String,
    val currency: String,
    val rows: List<PriceRow>,
)

/** One price, e.g. "PSA 10" or "Market". [salesCount] is only known for graded prices. */
data class PriceRow(
    val label: String,
    val amount: Double,
    val salesCount: Int? = null,
)
