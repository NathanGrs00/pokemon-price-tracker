package com.nathan.pokemonpricetracker.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.nathan.pokemonpricetracker.data.CardRepository
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

    private suspend fun loadGradedPrices(card: CardDetail): GradedPrices {
        if (!repository.hasPriceTrackerApiKey) return GradedPrices.MissingApiKey
        val tcgPlayerId = card.tcgPlayerId ?: return GradedPrices.NotAvailable
        return try {
            repository.getGradedPrices(tcgPlayerId)
                ?.let { GradedPrices.Loaded(it) }
                ?: GradedPrices.NotAvailable
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            GradedPrices.Error
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CardDetailViewModel(createSavedStateHandle(), CardRepository()) }
        }
    }
}
