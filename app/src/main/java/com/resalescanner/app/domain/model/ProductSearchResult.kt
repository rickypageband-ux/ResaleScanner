package com.resalescanner.app.domain.model

data class RetailPrice(val retailer: String, val priceCents: Long)

data class ProductSearchResult(
    val title: String,
    val query: String,
    val retailPrices: List<RetailPrice>,
    val estimatedSoldPriceCents: Long,
    val suggestedResalePriceCents: Long,
    val isSampleData: Boolean,
    val observedLowestPriceCents: Long? = null,
    val observedHighestPriceCents: Long? = null,
    val observedAveragePriceCents: Long? = null,
    val providerMessage: String? = null,
) {
    val lowestPriceCents get() = observedLowestPriceCents ?: retailPrices.minOfOrNull { it.priceCents } ?: 0
    val highestPriceCents get() = observedHighestPriceCents ?: retailPrices.maxOfOrNull { it.priceCents } ?: 0
    val averagePriceCents get() = observedAveragePriceCents ?: retailPrices.map { it.priceCents }.average().takeUnless(Double::isNaN)?.toLong() ?: 0
}

/** Sprint 1 stand-in for future authenticated retailer and sold-listing providers. */
fun sampleSearchResult(query: String) = ProductSearchResult(
    title = if (query.any(Char::isLetter)) query.replaceFirstChar(Char::uppercase) else "Milwaukee M18 Drill",
    query = query,
    retailPrices = listOf(
        RetailPrice("Amazon", 14_999),
        RetailPrice("Walmart", 13_800),
        RetailPrice("Home Depot", 15_900),
        RetailPrice("Google Shopping", 14_700),
    ),
    estimatedSoldPriceCents = 12_000,
    suggestedResalePriceCents = 13_500,
    isSampleData = true,
)
