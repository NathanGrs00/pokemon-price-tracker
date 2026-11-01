package com.nathan.pokemonpricetracker.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/** Pokémon cards are 63 × 88 mm. */
private const val CARD_ASPECT_RATIO = 63f / 88f

/** A card picture with a card-shaped placeholder while it loads or when there is no image. */
@Composable
fun CardImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) }

    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.small),
        placeholder = placeholder,
        error = placeholder,
        contentScale = ContentScale.Fit,
    )
}
