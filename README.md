# Pokémon Price Tracker

Android app (Jetpack Compose + Material 3) for quick Pokémon card price lookups.
Search any card, tap it, and see its prices by grade and printing.

## Where the prices come from

| Source | What you get | API key |
| --- | --- | --- |
| [TCGdex](https://tcgdex.dev) | Every card and image. TCGplayer prices (USD) per printing: Normal, Holofoil, Reverse Holofoil, 1st Edition. Cardmarket prices (EUR). | Not needed |
| [PokemonPriceTracker](https://www.pokemonpricetracker.com/api) | Graded prices from eBay sales: PSA 10, PSA 9, PSA 8, and more | Free key |

Search results load 30 cards per page while you scroll. Each request has a 30-second
limit, so a slow connection shows a retry button instead of loading forever.

## Setup

1. Open the project in Android Studio and run the `app` configuration.
2. Optional, for PSA graded prices: create a free account on PokemonPriceTracker,
   open any card in the app and paste your API key in the "Graded" box.
   The key is stored on the phone only.

   The free tier gives 100 credits a day, and opening a card costs 2 credits
   (about 50 card lookups a day). Without a key the app still shows all market prices.

## Project structure

```
app/src/main/java/com/nathan/pokemonpricetracker/
├── MainActivity.kt
├── data/
│   ├── CardRepository.kt      single entry point for the UI
│   ├── CardPagingSource.kt    paged search (Paging 3)
│   ├── local/                 API key stored on the phone
│   ├── mapper/                API responses -> app models
│   ├── model/                 app models (cards, price sections)
│   └── remote/                Retrofit APIs, DTOs, ApiClient
└── ui/
    ├── cards/                 search + card grid
    ├── detail/                card prices
    ├── components/            shared composables
    ├── navigation/            routes + NavHost
    └── theme/                 Material 3 theme
```

Run the unit tests with `./gradlew test`.
