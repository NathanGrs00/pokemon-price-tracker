package com.nathan.pokemonpricetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nathan.pokemonpricetracker.ui.cards.CardListScreen
import com.nathan.pokemonpricetracker.ui.detail.CardDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data object CardListRoute

@Serializable
data class CardDetailRoute(val cardId: String)

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = CardListRoute) {
        composable<CardListRoute> {
            CardListScreen(
                onCardClick = { cardId -> navController.navigate(CardDetailRoute(cardId)) },
            )
        }
        composable<CardDetailRoute> {
            CardDetailScreen(onBack = { navController.navigateUp() })
        }
    }
}
