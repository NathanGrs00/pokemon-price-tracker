package com.nathan.pokemonpricetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nathan.pokemonpricetracker.ui.navigation.AppNavHost
import com.nathan.pokemonpricetracker.ui.theme.PokemonPriceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokemonPriceTrackerTheme {
                AppNavHost()
            }
        }
    }
}
