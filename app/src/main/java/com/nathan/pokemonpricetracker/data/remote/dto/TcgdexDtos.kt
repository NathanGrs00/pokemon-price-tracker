package com.nathan.pokemonpricetracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Item of `GET /cards`. [image] is a base URL without quality and extension. */
@Serializable
data class TcgdexCardBriefDto(
    val id: String,
    val localId: String = "",
    val name: String,
    val image: String? = null,
)

/** Response of `GET /cards/{id}`. Only the fields the app uses. */
@Serializable
data class TcgdexCardDto(
    val id: String,
    val localId: String = "",
    val name: String,
    val image: String? = null,
    val rarity: String? = null,
    val set: TcgdexSetDto? = null,
    @SerialName("variants_detailed")
    val variantsDetailed: List<TcgdexVariantDto> = emptyList(),
    val pricing: TcgdexPricingDto? = null,
)

@Serializable
data class TcgdexSetDto(
    val name: String = "",
    val cardCount: TcgdexCardCountDto? = null,
)

@Serializable
data class TcgdexCardCountDto(
    val official: Int? = null,
)

@Serializable
data class TcgdexVariantDto(
    val thirdParty: TcgdexThirdPartyDto? = null,
)

@Serializable
data class TcgdexThirdPartyDto(
    val tcgplayer: Long? = null,
)

@Serializable
data class TcgdexPricingDto(
    /** "unit", "updated" and one object per printing ("normal", "holofoil", "reverse-holofoil", ...). */
    val tcgplayer: JsonObject? = null,
    val cardmarket: CardmarketPricesDto? = null,
)

@Serializable
data class TcgPlayerPriceDto(
    val productId: Long? = null,
    val lowPrice: Double? = null,
    val midPrice: Double? = null,
    val highPrice: Double? = null,
    val marketPrice: Double? = null,
)

@Serializable
data class CardmarketPricesDto(
    val unit: String? = null,
    val avg: Double? = null,
    val low: Double? = null,
    val trend: Double? = null,
    val avg30: Double? = null,
    @SerialName("avg-holo") val avgHolo: Double? = null,
    @SerialName("low-holo") val lowHolo: Double? = null,
    @SerialName("trend-holo") val trendHolo: Double? = null,
    @SerialName("avg30-holo") val avg30Holo: Double? = null,
)
