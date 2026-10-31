package com.nathan.pokemonpricetracker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.nathan.pokemonpricetracker.data.CardRepository
import com.nathan.pokemonpricetracker.data.local.ApiKeyStore
import com.nathan.pokemonpricetracker.data.model.CardDetail
import com.nathan.pokemonpricetracker.data.model.PriceSection
import com.nathan.pokemonpricetracker.ui.navigation.CardDetailRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CardDetailUiState {
    data object Loading : CardDetailUiState
    data object Error : CardDetailUiState
    data class Success(val card: CardDetail, val gradedPrices: GradedPrices) : CardDetailUiState
}

sealed interface GradedPrices {
    data object Loading : GradedPrices
    data object MissingApiKey : GradedPrices
    data object NotAvailable : GradedPrices
    data object Error : GradedPrices
    data class Loaded(val section: PriceSection) : GradedPrices
}

class CardDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CardRepository,
    private val apiKeyStore: ApiKeyStore,
) : ViewModel() {

    private val cardId = savedStateHandle.toRoute<CardDetailRoute>().cardId

    private val _uiState = MutableStateFlow<CardDetailUiState>(CardDetailUiState.Loading)
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    init {
        loadCard()
    }

    fun loadCard() {
        viewModelScope.launch {
            _uiState.value = CardDetailUiState.Loading
            val card = try {
                repository.getCard(cardId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = CardDetailUiState.Error
                return@launch
            }
            // Show market prices right away; graded prices come from a second, slower API.
            _uiState.value = CardDetailUiState.Success(card, GradedPrices.Loading)
            _uiState.value = CardDetailUiState.Success(card, loadGradedPrices(card))
        }
    }

    fun saveApiKey(apiKey: String) {
        apiKeyStore.priceTrackerApiKey = apiKey
        val state = _uiState.value as? CardDetailUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(gradedPrices = GradedPrices.Loading)
            _uiState.value = state.copy(gradedPrices = loadGradedPrices(state.card))
        }
    }

    private suspend fun loadGradedPrices(card: CardDetail): GradedPrices {
        val apiKey = apiKeyStore.priceTrackerApiKey
        if (apiKey.isBlank()) return GradedPrices.MissingApiKey
        val tcgPlayerId = card.tcgPlayerId ?: return GradedPrices.NotAvailable
        return try {
            repository.getGradedPrices(tcgPlayerId, apiKey)
                ?.let { GradedPrices.Loaded(it) }
                ?: GradedPrices.NotAvailable
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            GradedPrices.Error
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                CardDetailViewModel(createSavedStateHandle(), CardRepository(), ApiKeyStore(app))
            }
        }
    }
}
