package com.nathan.pokemonpricetracker.data.remote

import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * PokemonPriceTracker: graded (PSA) prices from eBay sales. The free tier needs an API key
 * and gives 100 credits a day; one lookup with eBay data costs 2 credits.
 * https://www.pokemonpricetracker.com/api
 */
interface PriceTrackerApi {

    /** Returns `{ "data": ... }`, where data is a list of cards or a single card. */
    @GET("cards?includeEbay=true")
    suspend fun getCard(
        @Header("Authorization") authorization: String,
        @Query("tcgPlayerId") tcgPlayerId: Long,
    ): JsonObject
}
