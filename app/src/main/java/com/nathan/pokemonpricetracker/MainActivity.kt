package com.nathan.pokemonpricetracker

import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nathan.pokemonpricetracker.data.model.Pokemon
import com.nathan.pokemonpricetracker.data.remote.APIController

class MainActivity : AppCompatActivity() {

    private lateinit var lstPokemons: GridView // Declare GridView
    private lateinit var pokemons: ArrayList<Pokemon> // Declare the list of Pokémon
    private lateinit var adapter: PokemonAdapter // Declare the custom adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Set window insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the GridView
        lstPokemons = findViewById(R.id.gv_pokemons)

        // Fetch the Pokémon data asynchronously
        val ac = APIController(this)
        ac.getData { fetchedPokemons ->
            // Ensure the pokemons list is populated
            pokemons = fetchedPokemons

            // Initialize the custom adapter with the fetched data
            adapter = PokemonAdapter(this, pokemons)
            // Set the adapter to the GridView
            lstPokemons.adapter = adapter
        }
    }
}
