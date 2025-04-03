package com.nathan.pokemonpricetracker.data.model

import com.nathan.pokemonpricetracker.data.remote.Images


class Pokemon (var name: String, var images: Images) {
    override fun toString(): String {
        return "$name + ${images.small}"
    }
}