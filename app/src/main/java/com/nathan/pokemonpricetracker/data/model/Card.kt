package com.nathan.pokemonpricetracker.data.model

/** A card as shown in the search grid. */
data class CardSummary(
    val id: String,
    val name: String,
    val imageUrl: String?,
)

/** A card with all the market prices found for it. */
data class CardDetail(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val setName: String,
    val number: String,
    val rarity: String?,
    /** Used to look up graded prices. Null when the card isn't sold on TCGplayer. */
    val tcgPlayerId: Long?,
    val marketPrices: List<PriceSection>,
)
