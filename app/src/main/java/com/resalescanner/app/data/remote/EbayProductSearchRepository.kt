package com.resalescanner.app.data.remote

import com.resalescanner.app.domain.model.ProductSearchResult
import com.resalescanner.app.domain.model.RetailPrice
import com.resalescanner.app.domain.repository.ProductSearchRepository
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EbayProductSearchRepository(
    private val endpoint: String,
    private val publishableKey: String,
) : ProductSearchRepository {
    override suspend fun search(query: String): ProductSearchResult = withContext(Dispatchers.IO) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("apikey", publishableKey)
            setRequestProperty("Authorization", "Bearer $publishableKey")
        }
        try {
            connection.outputStream.bufferedWriter().use { it.write(JSONObject().put("query", query).toString()) }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = JSONObject(body.ifBlank { "{}" })
            if (status !in 200..299) throw IllegalStateException(json.optString("error", "Live eBay search failed"))

            val items = json.optJSONArray("items")
            if (items == null || items.length() == 0) throw NoSuchElementException("No eBay listings found for this item")
            val first = items.getJSONObject(0)
            val summary = json.getJSONObject("summary")
            val averageCents = summary.getDouble("average").dollarsToCents()
            ProductSearchResult(
                title = first.optString("title", query),
                query = query,
                retailPrices = listOf(RetailPrice("eBay active listings", averageCents)),
                estimatedSoldPriceCents = 0,
                suggestedResalePriceCents = (averageCents * 0.9).toLong(),
                isSampleData = false,
                observedLowestPriceCents = summary.getDouble("lowest").dollarsToCents(),
                observedHighestPriceCents = summary.getDouble("highest").dollarsToCents(),
                observedAveragePriceCents = averageCents,
                providerMessage = "Live eBay active listings (${items.length()} compared)",
            )
        } finally {
            connection.disconnect()
        }
    }
}

private fun Double.dollarsToCents() = (this * 100).toLong()
