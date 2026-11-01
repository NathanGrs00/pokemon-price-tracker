package com.nathan.pokemonpricetracker.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** A card from PokemonPriceTracker. Only the graded eBay sales are used. */
@Serializable
data class PriceTrackerCardDto(
    val ebay: EbayDto? = null,
)

@Serializable
data class EbayDto(
    /** One object per grade, keyed like "psa10" or "psa9". */
    val salesByGrade: JsonObject? = null,
)

@Serializable
data class GradeSalesDto(
    val count: Int? = null,
    val averagePrice: Double? = null,
    val medianPrice: Double? = null,
    val marketPrice7Day: Double? = null,
    val smartMarketPrice: SmartMarketPriceDto? = null,
) {
    /** Best available price estimate, most reliable first. */
    val price: Double?
        get() = (smartMarketPrice?.price ?: marketPrice7Day ?: medianPrice ?: averagePrice)
            ?.takeIf { it > 0 }
}

@Serializable
data class SmartMarketPriceDto(
    val price: Double? = null,
)
