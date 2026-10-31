package com.nathan.pokemonpricetracker.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nathan.pokemonpricetracker.data.mapper.toCardSummary
import com.nathan.pokemonpricetracker.data.model.CardSummary
import com.nathan.pokemonpricetracker.data.remote.TcgdexApi
import kotlinx.coroutines.CancellationException

/** Loads search results one small page at a time. TCGdex pages start at 1. */
class CardPagingSource(
    private val api: TcgdexApi,
    private val query: String,
) : PagingSource<Int, CardSummary>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CardSummary> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val cards = api.getCards(name = query.ifBlank { null }, page = page, pageSize = PAGE_SIZE)
            LoadResult.Page(
                data = cards.map { it.toCardSummary() },
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                // A short page means there is nothing more to load.
                nextKey = if (cards.size < PAGE_SIZE) null else page + 1,
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // Network, HTTP and parsing errors all become an error state with a retry button.
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CardSummary>): Int? =
        state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }

    companion object {
        const val PAGE_SIZE = 30
        private const val FIRST_PAGE = 1
    }
}
