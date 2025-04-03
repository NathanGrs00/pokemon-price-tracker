package com.nathan.pokemonpricetracker.data.remote

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nathan.pokemonpricetracker.data.model.Pokemon
import org.json.JSONObject

class APIController(var context: Context, var pokemonName: String) {
    fun getData(callback: (ArrayList<Pokemon>) -> Unit){
        val url = "https://api.pokemontcg.io/v2/cards?q=name:$pokemonName"
        val gson = Gson()
        val queue: RequestQueue = Volley.newRequestQueue(context)

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val jsonArray = JSONObject(response.toString()).getJSONArray("data")
                val arrayPokemon = object : TypeToken<List<Pokemon>>() {}.type
                val pokemons: ArrayList<Pokemon> = gson.fromJson(jsonArray.toString(), arrayPokemon)
                callback(pokemons)
            },
            { error ->
                error.printStackTrace()
            }
        )
        queue.add(request)
    }
}
