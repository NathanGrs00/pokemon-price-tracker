package com.nathan.pokemonpricetracker.data.remote

import com.nathan.pokemonpricetracker.data.remote.dto.TcgdexCardBriefDto
import com.nathan.pokemonpricetracker.data.remote.dto.TcgdexCardDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TCGdex: free, no API key. Every Pokémon card with TCGplayer and Cardmarket prices. https://tcgdex.dev */
interface TcgdexApi {

    /** One page of cards. [name] is a case-insensitive "contains" filter; null lists every card. */
    @GET("cards")
    suspend fun getCards(
        @Query("name") name: String?,
        @Query("pagination:page", encoded = true) page: Int,
        @Query("pagination:itemsPerPage", encoded = true) pageSize: Int,
    ): List<TcgdexCardBriefDto>

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") id: String): TcgdexCardDto
}
