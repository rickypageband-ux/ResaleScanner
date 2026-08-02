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
}
