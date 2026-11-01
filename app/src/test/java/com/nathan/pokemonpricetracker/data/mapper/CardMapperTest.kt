package com.nathan.pokemonpricetracker.data.mapper

import com.nathan.pokemonpricetracker.data.model.PriceRow
import com.nathan.pokemonpricetracker.data.model.PriceSection
import com.nathan.pokemonpricetracker.data.remote.ApiClient
import com.nathan.pokemonpricetracker.data.remote.dto.TcgdexCardBriefDto
import com.nathan.pokemonpricetracker.data.remote.dto.TcgdexCardDto
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardMapperTest {

    @Test
    fun `brief card gets a small image url`() {
        val brief = ApiClient.json.decodeFromString<TcgdexCardBriefDto>(
            """{ "id": "base1-4", "localId": "4", "name": "Charizard", "image": "https://assets.tcgdex.net/en/base/base1/4" }"""
        )

        val summary = brief.toCardSummary()

        assertEquals("https://assets.tcgdex.net/en/base/base1/4/low.webp", summary.imageUrl)
    }

    @Test
    fun `card detail lists prices per printing and source`() {
        val card = ApiClient.json.decodeFromString<TcgdexCardDto>(TCGDEX_CARD).toCardDetail()

        assertEquals("Base Set", card.setName)
        assertEquals("4/102", card.number)
        assertEquals(42382L, card.tcgPlayerId)
        assertEquals(
            listOf(
                PriceSection(
                    "TCGplayer · Holofoil", "USD",
                    listOf(PriceRow("Market", 350.5), PriceRow("Low", 300.0), PriceRow("Mid", 380.0), PriceRow("High", 999.99)),
                ),
                PriceSection(
                    "TCGplayer · 1st Edition Holofoil", "USD",
                    listOf(PriceRow("Market", 5000.0)),
                ),
                PriceSection(
                    "Cardmarket", "EUR",
                    listOf(PriceRow("Trend", 310.0), PriceRow("Average", 290.0), PriceRow("Low", 150.0), PriceRow("30-day average", 300.0)),
                ),
            ),
            card.marketPrices,
        )
    }

    @Test
    fun `tcgplayer id falls back to the price product id`() {
        val card = ApiClient.json.decodeFromString<TcgdexCardDto>(
            """
            {
              "id": "sv1-1", "localId": "1", "name": "Pineco",
              "pricing": { "tcgplayer": { "unit": "USD", "normal": { "productId": 1234, "marketPrice": 0.1 } } }
            }
            """
        ).toCardDetail()

        assertEquals(1234L, card.tcgPlayerId)
        assertEquals("1", card.number)
    }

    @Test
    fun `card without pricing has no prices`() {
        val card = ApiClient.json.decodeFromString<TcgdexCardDto>(
            """{ "id": "x-1", "name": "Pikachu", "localId": null, "set": { "name": "Promo", "cardCount": { "official": 50 } } }"""
        ).toCardDetail()

        assertEquals("", card.number)
        assertEquals(emptyList<PriceSection>(), card.marketPrices)
        assertNull(card.tcgPlayerId)
        assertNull(card.imageUrl)
    }

    @Test
    fun `graded prices are sorted PSA first and highest grade first`() {
        val response = ApiClient.json.decodeFromString<JsonObject>(GRADED_RESPONSE)

        val section = response.toGradedPriceSection()

        assertEquals(
            listOf(
                PriceRow("PSA 10", 3150.0, salesCount = 12),
                PriceRow("PSA 9", 900.0, salesCount = 30),
                PriceRow("PSA 8", 450.0, salesCount = 8),
                PriceRow("BGS 9.5", 1200.0, salesCount = 2),
            ),
            section?.rows,
        )
    }

    @Test
    fun `graded prices also work when data is a single card`() {
        // Sample response from the PokemonPriceTracker docs (tcgPlayerId lookup).
        val response = ApiClient.json.decodeFromString<JsonObject>(
            """
            {
              "data": {
                "name": "Charizard",
                "setName": "Base Set",
                "ebay": {
                  "salesByGrade": {
                    "psa10": { "count": 1, "medianPrice": 17500, "lastSaleDate": "2025-10-30T00:00:00.000Z" },
                    "psa9": { "count": 19, "medianPrice": 2487.5, "lastSaleDate": "2026-07-30T00:00:00.000Z" }
                  },
                  "soldListings": { "psa9": [ { "price": 2200, "soldDate": "2026-01-01T00:00:00.000Z", "grade": 9 } ] },
                  "totalSales": 415,
                  "salesVelocity": { "dailyAverage": 0.6, "monthlyTotal": 18 }
                }
              }
            }
            """
        )

        assertEquals(
            listOf(PriceRow("PSA 10", 17500.0, salesCount = 1), PriceRow("PSA 9", 2487.5, salesCount = 19)),
            response.toGradedPriceSection()?.rows,
        )
    }

    @Test
    fun `no graded sales gives no section`() {
        val response = ApiClient.json.decodeFromString<JsonObject>("""{ "data": [] }""")

        assertNull(response.toGradedPriceSection())
    }

    @Test
    fun `labels are readable`() {
        assertEquals("PSA 10", "psa10".toGradeLabel())
        assertEquals("CGC 9.5", "cgc9_5".toGradeLabel())
        assertEquals("BGS 10", "bgs10".toGradeLabel())
        assertEquals("raw", "raw".toGradeLabel())
        assertEquals("Reverse Holofoil", "reverse-holofoil".toPrintingLabel())
    }

    private companion object {
        val TCGDEX_CARD = """
            {
              "id": "base1-4",
              "localId": "4",
              "name": "Charizard",
              "image": "https://assets.tcgdex.net/en/base/base1/4",
              "rarity": "Rare",
              "category": "Pokemon",
              "set": { "id": "base1", "name": "Base Set", "cardCount": { "official": 102, "total": 102 } },
              "variants_detailed": [
                { "type": "holo", "thirdParty": { "cardmarket": 273699, "tcgplayer": 42382 } }
              ],
              "pricing": {
                "cardmarket": {
                  "updated": "2025-08-05T00:42:15.000Z",
                  "unit": "EUR",
                  "idProduct": 273699,
                  "avg": 290.0, "low": 150.0, "trend": 310.0, "avg1": 280.0, "avg7": 295.0, "avg30": 300.0,
                  "avg-holo": null, "low-holo": 0, "trend-holo": null
                },
                "tcgplayer": {
                  "updated": "2025-08-05T20:07:54.000Z",
                  "unit": "USD",
                  "holofoil": { "productId": 42382, "lowPrice": 300.0, "midPrice": 380.0, "highPrice": 999.99, "marketPrice": 350.5, "directLowPrice": null },
                  "1st-edition-holofoil": { "marketPrice": 5000.0 }
                }
              }
            }
        """.trimIndent()

        val GRADED_RESPONSE = """
            {
              "data": [
                {
                  "name": "Charizard",
                  "tcgPlayerId": "42382",
                  "ebay": {
                    "updatedAt": "2025-08-01",
                    "salesByGrade": {
                      "psa8": { "count": 8, "medianPrice": 450.0 },
                      "bgs9_5": { "count": 2, "averagePrice": 1200.0 },
                      "psa10": { "count": 12, "averagePrice": 3000.0, "smartMarketPrice": { "price": 3150.0, "confidence": "high" } },
                      "psa9": { "count": 30, "marketPrice7Day": 900.0, "averagePrice": 950.0 },
                      "psa7": { "count": 0 }
                    }
                  }
                }
              ],
              "metadata": { "total": 1, "hasMore": false }
            }
        """.trimIndent()
    }
}
