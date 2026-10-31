package com.nathan.pokemonpricetracker.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nathan.pokemonpricetracker.BuildConfig
import com.nathan.pokemonpricetracker.data.mapper.toCardDetail
import com.nathan.pokemonpricetracker.data.mapper.toGradedPriceSection
import com.nathan.pokemonpricetracker.data.model.CardDetail
import com.nathan.pokemonpricetracker.data.model.CardSummary
import com.nathan.pokemonpricetracker.data.model.PriceSection
import com.nathan.pokemonpricetracker.data.remote.ApiClient
import com.nathan.pokemonpricetracker.data.remote.PriceTrackerApi
import com.nathan.pokemonpricetracker.data.remote.TcgdexApi
import kotlinx.coroutines.flow.Flow

class CardRepository(
    private val tcgdexApi: TcgdexApi = ApiClient.tcgdexApi,
    private val priceTrackerApi: PriceTrackerApi = ApiClient.priceTrackerApi,
    private val priceTrackerApiKey: String = BuildConfig.PRICE_TRACKER_API_KEY,
) {

    val hasPriceTrackerApiKey: Boolean
        get() = priceTrackerApiKey.isNotBlank()

    fun searchCards(query: String): Flow<PagingData<CardSummary>> = Pager(
        config = PagingConfig(pageSize = CardPagingSource.PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { CardPagingSource(tcgdexApi, query) },
    ).flow

    suspend fun getCard(id: String): CardDetail = tcgdexApi.getCard(id).toCardDetail()

    /** PSA (and other) graded prices, or null when no graded sales are known. */
    suspend fun getGradedPrices(tcgPlayerId: Long): PriceSection? =
        priceTrackerApi.getCard("Bearer $priceTrackerApiKey", tcgPlayerId).toGradedPriceSection()
}
