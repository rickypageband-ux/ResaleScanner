package com.resalescanner.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProductSearchResultTest {
    @Test
    fun derivesRetailSummaryPrices() {
        val result = ProductSearchResult(
            title = "Test item",
            query = "test",
            retailPrices = listOf(RetailPrice("A", 10_000), RetailPrice("B", 14_000), RetailPrice("C", 15_000)),
            estimatedSoldPriceCents = 9_000,
            suggestedResalePriceCents = 12_000,
            isSampleData = true,
        )

        assertEquals(10_000, result.lowestPriceCents)
        assertEquals(15_000, result.highestPriceCents)
        assertEquals(13_000, result.averagePriceCents)
    }

    @Test
    fun usesObservedMarketplaceSummaryWhenProvided() {
        val result = ProductSearchResult(
            title = "Live item",
            query = "live",
            retailPrices = listOf(RetailPrice("eBay", 12_000)),
            estimatedSoldPriceCents = 0,
            suggestedResalePriceCents = 11_000,
            isSampleData = false,
            observedLowestPriceCents = 8_000,
            observedHighestPriceCents = 16_000,
            observedAveragePriceCents = 12_500,
        )

        assertEquals(8_000, result.lowestPriceCents)
        assertEquals(16_000, result.highestPriceCents)
        assertEquals(12_500, result.averagePriceCents)
    }
}
