package com.nathan.pokemonpricetracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nathan.pokemonpricetracker.data.remote.APIController
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    lateinit var lstPokemons: GridView
    lateinit var adapter: ArrayAdapter<Pokemon>
    lateinit var pokemons: ArrayList<Pokemon>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lstPokemons = findViewById(R.id.gv_pokemons)

        val ac = APIController(this)
        ac.getData { fetchedPokemons ->
            pokemons = fetchedPokemons
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pokemons)
            lstPokemons.adapter = adapter
        }

    }
}