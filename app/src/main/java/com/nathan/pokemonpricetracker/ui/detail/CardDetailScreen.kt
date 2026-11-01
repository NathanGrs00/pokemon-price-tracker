package com.nathan.pokemonpricetracker.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nathan.pokemonpricetracker.R
import com.nathan.pokemonpricetracker.data.model.CardDetail
import com.nathan.pokemonpricetracker.data.model.PriceSection
import com.nathan.pokemonpricetracker.ui.components.CardImage
import com.nathan.pokemonpricetracker.ui.components.ErrorMessage
import com.nathan.pokemonpricetracker.ui.components.LoadingIndicator
import com.nathan.pokemonpricetracker.ui.components.MessageText
import java.text.NumberFormat
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    onBack: () -> Unit,
    viewModel: CardDetailViewModel = viewModel(factory = CardDetailViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = (uiState as? CardDetailUiState.Success)?.card?.name.orEmpty()
                    Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        val modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        when (val state = uiState) {
            CardDetailUiState.Loading -> LoadingIndicator(modifier)
            CardDetailUiState.Error -> ErrorMessage(
                message = stringResource(R.string.error_loading_card),
                onRetry = viewModel::loadCard,
                modifier = modifier,
            )
            is CardDetailUiState.Success -> CardDetailContent(
                card = state.card,
                gradedPrices = state.gradedPrices,
                onSaveApiKey = viewModel::saveApiKey,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun CardDetailContent(
    card: CardDetail,
    gradedPrices: GradedPrices,
    onSaveApiKey: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CardHeader(card) }
        item { GradedPricesCard(gradedPrices, onSaveApiKey) }
        if (card.marketPrices.isEmpty()) {
            item { MessageText(stringResource(R.string.no_market_prices), Modifier.fillMaxWidth()) }
        }
        items(card.marketPrices) { section ->
            PriceCard(title = section.title) { PriceRows(section) }
        }
    }
}

@Composable
private fun CardHeader(card: CardDetail) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CardImage(
            imageUrl = card.imageUrl,
            contentDescription = card.name,
            modifier = Modifier.width(140.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(card.name, style = MaterialTheme.typography.headlineSmall)
            Text(card.setName, style = MaterialTheme.typography.bodyLarge)
            if (card.number.isNotBlank()) {
                Text(
                    text = "#${card.number}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            card.rarity?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GradedPricesCard(gradedPrices: GradedPrices, onSaveApiKey: (String) -> Unit) {
    PriceCard(title = stringResource(R.string.graded_prices)) {
        when (gradedPrices) {
            GradedPrices.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
            GradedPrices.MissingApiKey -> {
                InfoText(stringResource(R.string.graded_missing_api_key))
                ApiKeyField(onSave = onSaveApiKey)
            }
            GradedPrices.NotAvailable -> InfoText(stringResource(R.string.graded_not_available))
            GradedPrices.Error -> {
                InfoText(stringResource(R.string.graded_error))
                ApiKeyField(onSave = onSaveApiKey)
            }
            is GradedPrices.Loaded -> PriceRows(gradedPrices.section)
        }
    }
}

/** Lets the user paste their PokemonPriceTracker key on the phone itself. */
@Composable
private fun ApiKeyField(onSave: (String) -> Unit) {
    var apiKey by rememberSaveable { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current

    OutlinedTextField(
        value = apiKey,
        onValueChange = { apiKey = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.api_key_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (apiKey.isNotBlank()) onSave(apiKey) }),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        TextButton(onClick = { uriHandler.openUri(PRICE_TRACKER_SIGN_UP_URL) }) {
            Text(stringResource(R.string.get_api_key))
        }
        Button(onClick = { onSave(apiKey) }, enabled = apiKey.isNotBlank()) {
            Text(stringResource(R.string.save))
        }
    }
}

private const val PRICE_TRACKER_SIGN_UP_URL = "https://www.pokemonpricetracker.com/api-keys"

@Composable
private fun PriceCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
private fun PriceRows(section: PriceSection) {
    section.rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(row.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            row.salesCount?.let { count ->
                Text(
                    text = pluralStringResource(R.plurals.sales_count, count, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            Text(
                text = formatPrice(row.amount, section.currency),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun formatPrice(amount: Double, currencyCode: String): String = try {
    NumberFormat.getCurrencyInstance()
        .apply { currency = Currency.getInstance(currencyCode) }
        .format(amount)
} catch (e: IllegalArgumentException) {
    "%.2f %s".format(amount, currencyCode)
}
