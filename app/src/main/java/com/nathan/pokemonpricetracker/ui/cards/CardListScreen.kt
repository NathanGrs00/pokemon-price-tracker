package com.nathan.pokemonpricetracker.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.nathan.pokemonpricetracker.R
import com.nathan.pokemonpricetracker.data.model.CardSummary
import com.nathan.pokemonpricetracker.ui.components.CardImage
import com.nathan.pokemonpricetracker.ui.components.ErrorMessage
import com.nathan.pokemonpricetracker.ui.components.LoadingIndicator
import com.nathan.pokemonpricetracker.ui.components.MessageText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    onCardClick: (cardId: String) -> Unit,
    viewModel: CardListViewModel = viewModel(factory = CardListViewModel.Factory),
) {
    val cards = viewModel.cards.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text(stringResource(R.string.app_name)) })
                SearchField(
                    query = viewModel.query,
                    onQueryChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        },
    ) { innerPadding ->
        CardGrid(
            cards = cards,
            onCardClick = onCardClick,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
    )
}

@Composable
private fun CardGrid(
    cards: LazyPagingItems<CardSummary>,
    onCardClick: (cardId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorText = stringResource(R.string.error_loading_cards)

    when {
        cards.loadState.refresh is LoadState.Loading -> LoadingIndicator(modifier)
        cards.loadState.refresh is LoadState.Error -> ErrorMessage(errorText, onRetry = cards::retry, modifier = modifier)
        cards.itemCount == 0 -> MessageText(stringResource(R.string.no_cards_found), modifier)
        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            modifier = modifier,
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(count = cards.itemCount, key = cards.itemKey { it.id }) { index ->
                cards[index]?.let { card ->
                    CardGridItem(card = card, onClick = { onCardClick(card.id) })
                }
            }

            // Footer for the next page: a spinner while it loads, a retry button if it failed.
            when (cards.loadState.append) {
                is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingIndicator(Modifier.fillMaxWidth().padding(16.dp))
                }
                is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    ErrorMessage(errorText, onRetry = cards::retry, modifier = Modifier.fillMaxWidth())
                }
                is LoadState.NotLoading -> Unit
            }
        }
    }
}

@Composable
private fun CardGridItem(card: CardSummary, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick) {
        CardImage(
            imageUrl = card.imageUrl,
            contentDescription = card.name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
        Text(
            text = card.name,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Text(
            text = card.id,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
        )
    }
}
