<img src="app/src/main/ic_launcher-playstore.png" alt="App icon" width="96" />

# Pokémon Price Tracker

Android app (Jetpack Compose + Material 3) for quick Pokémon card price lookups.
Search any card, tap it, and see its prices by grade and printing.

## Install

1. On your Android phone (Android 7.0 or newer), open the
   [latest release](https://github.com/NathanGrs00/pokemon-price-tracker/releases/latest)
   and download the `.apk` file.
2. Open it and tap **Install**. If Android asks, allow installing apps from your browser.

When updating, Android may refuse to install over the old version, because releases are
signed with the build server's key, which can change. Uninstall the old version first.

## Where the prices come from

| Source | What you get | API key |
| --- | --- | --- |
| [TCGdex](https://tcgdex.dev) | Every card and image. TCGplayer prices (USD) per printing: Normal, Holofoil, Reverse Holofoil, 1st Edition. Cardmarket prices (EUR). | Not needed |
| [PokemonPriceTracker](https://www.pokemonpricetracker.com/api) | Graded prices from eBay sales: PSA 10, PSA 9, PSA 8, and more | Free key |

Search results load 30 cards per page while you scroll. Each request has a 30-second
limit, so a slow connection shows a retry button instead of loading forever.

## PSA graded prices

Create a free account on PokemonPriceTracker (the app has a "Get a free key" button),
open any card and paste your API key in the "Graded" box. The key is stored on the phone only.

The free tier gives 100 credits a day, and opening a card costs 2 credits
(about 50 card lookups a day). Without a key the app still shows all market prices.

## Development

Open the project in Android Studio and run the `app` configuration.
Run the unit tests with `./gradlew test`.

GitHub Actions builds and tests every push. To publish a release, bump `versionCode` and
`versionName` in `app/build.gradle.kts` and push a tag such as `v1.0.1`: the workflow
attaches the APK to a new GitHub release.

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
