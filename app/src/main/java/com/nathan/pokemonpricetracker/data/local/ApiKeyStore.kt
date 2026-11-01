package com.nathan.pokemonpricetracker.data.local

import android.content.Context
import androidx.core.content.edit

/** Keeps the PokemonPriceTracker API key on the device, so it can be pasted in from the app. */
class ApiKeyStore(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var priceTrackerApiKey: String
        get() = prefs.getString(KEY_PRICE_TRACKER, null).orEmpty()
        set(value) = prefs.edit { putString(KEY_PRICE_TRACKER, value.trim()) }

    private companion object {
        const val KEY_PRICE_TRACKER = "price_tracker_api_key"
    }
}
