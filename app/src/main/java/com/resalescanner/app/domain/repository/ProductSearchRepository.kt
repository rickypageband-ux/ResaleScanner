package com.resalescanner.app.domain.repository

import com.resalescanner.app.domain.model.ProductSearchResult

interface ProductSearchRepository {
    suspend fun search(query: String): ProductSearchResult
}

