package com.nathan.pokemonpricetracker.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object ApiClient {

    /** The APIs return many fields the app doesn't need, and sometimes null for ones it does. */
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // Hard limit per request, so a slow server shows an error instead of loading forever.
    private val httpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    val tcgdexApi: TcgdexApi by lazy {
        retrofit("https://api.tcgdex.net/v2/en/").create()
    }

    val priceTrackerApi: PriceTrackerApi by lazy {
        retrofit("https://www.pokemonpricetracker.com/api/v2/").create()
    }

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
