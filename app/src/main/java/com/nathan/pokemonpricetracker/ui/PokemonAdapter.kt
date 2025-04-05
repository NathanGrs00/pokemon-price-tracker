package com.nathan.pokemonpricetracker.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.nathan.pokemonpricetracker.R
import com.nathan.pokemonpricetracker.data.model.Pokemon
import com.squareup.picasso.Picasso


class PokemonAdapter(context: Context, private val pokemons: List<Pokemon>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = pokemons.size

    override fun getItem(position: Int): Any = pokemons[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: inflater.inflate(R.layout.grid_item, parent, false)

        val pokemon = pokemons[position]

        val imageView: ImageView = view.findViewById(R.id.pokemon_image)
        val nameTextView: TextView = view.findViewById(R.id.pokemon_name)

        // Set the Pokémon name
        nameTextView.text = pokemon.name

        Picasso.get().load(pokemon.images.small).into(imageView)

        return view
    }
}
