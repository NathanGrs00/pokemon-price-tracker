package com.nathan.pokemonpricetracker.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nathan.pokemonpricetracker.R
import com.squareup.picasso.Picasso

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_view)

        val name = intent.getStringExtra("name")
        val image = intent.getStringExtra("image")

        val pokemonName = findViewById<TextView>(R.id.tvPokemonName)
        val pokemonImage = findViewById<ImageView>(R.id.ivPokemonImage)

        pokemonName.text = name
        Picasso.get()
            .load(image)
            .into(pokemonImage)
    }
}