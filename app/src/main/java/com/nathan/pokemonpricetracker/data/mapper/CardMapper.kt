package com.nathan.pokemonpricetracker.data.mapper

import com.nathan.pokemonpricetracker.data.model.CardDetail
import com.nathan.pokemonpricetracker.data.model.CardSummary
import com.nathan.pokemonpricetracker.data.model.PriceRow
import com.nathan.pokemonpricetracker.data.model.PriceSection
import com.nathan.pokemonpricetracker.data.remote.ApiClient
import com.nathan.pokemonpricetracker.data.remote.dto.CardmarketPricesDto
import com.nathan.pokemonpricetracker.data.remote.dto.GradeSalesDto
import com.nathan.pokemonpricetracker.data.remote.dto.PriceTrackerCardDto
import com.nathan.pokemonpricetracker.data.remote.dto.TcgPlayerPriceDto
import com.nathan.pokemonpricetracker.data.remote.dto.TcgdexCardBriefDto
import com.nathan.pokemonpricetracker.data.remote.dto.TcgdexCardDto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement

fun TcgdexCardBriefDto.toCardSummary() = CardSummary(
    id = id,
    name = name,
    imageUrl = image?.let { "$it/low.webp" },
)

fun TcgdexCardDto.toCardDetail(): CardDetail {
    val tcgPlayerPrices = pricing?.tcgplayer.decodeObjectValues<TcgPlayerPriceDto>()
    val tcgPlayerCurrency = (pricing?.tcgplayer?.get("unit") as? JsonPrimitive)?.contentOrNull ?: "USD"

    return CardDetail(
        id = id,
        name = name,
        imageUrl = image?.let { "$it/high.webp" },
        setName = set?.name.orEmpty(),
        number = set?.cardCount?.official?.takeIf { localId.isNotBlank() }?.let { "$localId/$it" } ?: localId,
        rarity = rarity,
        tcgPlayerId = variantsDetailed.firstNotNullOfOrNull { it.thirdParty?.tcgplayer }
            ?: tcgPlayerPrices.values.firstNotNullOfOrNull { it.productId },
        marketPrices = tcgPlayerSections(tcgPlayerPrices, tcgPlayerCurrency) +
            pricing?.cardmarket?.toSections().orEmpty(),
    )
}

/** Reads graded eBay sales from a PokemonPriceTracker response, or null when there are none. */
fun JsonObject.toGradedPriceSection(): PriceSection? {
    val data = this["data"]
    val card = (if (data is JsonArray) data.firstOrNull() else data) as? JsonObject ?: return null
    val salesByGrade = ApiClient.json.decodeFromJsonElement<PriceTrackerCardDto>(card)
        .ebay?.salesByGrade.decodeObjectValues<GradeSalesDto>()

    val rows = salesByGrade
        .mapNotNull { (grade, sales) ->
            sales.price?.let { PriceRow(grade.toGradeLabel(), it, sales.count) }
        }
        .sortedWith(compareBy<PriceRow>({ !it.label.startsWith("PSA") }, { -it.gradeNumber() }))

    return if (rows.isEmpty()) null else PriceSection("Graded · eBay sales", "USD", rows)
}

private fun tcgPlayerSections(
    prices: Map<String, TcgPlayerPriceDto>,
    currency: String,
): List<PriceSection> = prices.mapNotNull { (printing, price) ->
    section(
        "TCGplayer · ${printing.toPrintingLabel()}",
        currency,
        "Market" to price.marketPrice,
        "Low" to price.lowPrice,
        "Mid" to price.midPrice,
        "High" to price.highPrice,
    )
}

private fun CardmarketPricesDto.toSections(): List<PriceSection> {
    val currency = unit ?: "EUR"
    return listOfNotNull(
        section("Cardmarket", currency, "Trend" to trend, "Average" to avg, "Low" to low, "30-day average" to avg30),
        section("Cardmarket · Holo", currency, "Trend" to trendHolo, "Average" to avgHolo, "Low" to lowHolo, "30-day average" to avg30Holo),
    )
}

/** Builds a section from label/price pairs, skipping missing prices. Null when none are left. */
private fun section(title: String, currency: String, vararg prices: Pair<String, Double?>): PriceSection? {
    val rows = prices.mapNotNull { (label, amount) ->
        amount?.takeIf { it > 0 }?.let { PriceRow(label, it) }
    }
    return if (rows.isEmpty()) null else PriceSection(title, currency, rows)
}

/** Decodes the object values of a JSON object, skipping plain values like "unit" or "updated". */
private inline fun <reified T> JsonObject?.decodeObjectValues(): Map<String, T> =
    orEmpty()
        .filterValues { it is JsonObject }
        .mapValues { (_, value) -> ApiClient.json.decodeFromJsonElement<T>(value) }

private val gradeKey = Regex("""([a-z]+)[\s_-]?(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)

/** "psa10" -> "PSA 10", "bgs9.5" -> "BGS 9.5". Unknown formats are kept as they are. */
internal fun String.toGradeLabel(): String {
    val match = gradeKey.matchEntire(this) ?: return this
    val (grader, grade) = match.destructured
    return "${grader.uppercase()} $grade"
}

private fun PriceRow.gradeNumber(): Double = label.substringAfterLast(' ').toDoubleOrNull() ?: 0.0

/** "reverse-holofoil" -> "Reverse Holofoil" */
internal fun String.toPrintingLabel(): String =
    split('-').joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
