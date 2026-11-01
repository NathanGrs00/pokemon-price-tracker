package com.nathan.pokemonpricetracker.ui.cards

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nathan.pokemonpricetracker.data.CardRepository
import com.nathan.pokemonpricetracker.data.model.CardSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

class CardListViewModel(private val repository: CardRepository) : ViewModel() {

    /** Search text. Compose state (not a Flow) so the text field never lags behind typing. */
    var query by mutableStateOf("")
        private set

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val cards: Flow<PagingData<CardSummary>> = snapshotFlow { query.trim() }
        // Search once typing pauses; show all cards right away when the search is cleared.
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()
        .flatMapLatest { repository.searchCards(it) }
        .cachedIn(viewModelScope)

    fun onQueryChange(value: String) {
        query = value
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 400L

        val Factory = viewModelFactory {
            initializer { CardListViewModel(CardRepository()) }
        }
    }
}
