package com.bgvofir.productcatalog.testutil

import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.ErrorType
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.data.api.Product
import com.bgvofir.productcatalog.data.api.ProductsResponse
import com.bgvofir.productcatalog.data.api.sampleProduct
import com.bgvofir.productcatalog.repositories.IProductRepository
import kotlinx.coroutines.CompletableDeferred

// In-memory repository that records every call, so tests can assert what was requested.
class FakeProductRepository : IProductRepository {
    val calls = mutableListOf<String>()
    var products: List<Product> = (1..45).map { sampleProduct(id = it) }
    var categories: DataState<List<Category>> = DataState.Success(listOf(Category("beauty", "Beauty")))

    // The next product request (list, search, category or single product) fails once.
    var failNextProductCall = false

    // When set, requests for later pages (skip > 0) suspend until it completes, so tests can
    // change the filter while a page is still in flight.
    var nextPageGate: CompletableDeferred<Unit>? = null

    // The same for first-page requests (skip == 0), e.g. to observe a refresh while it runs.
    var firstPageGate: CompletableDeferred<Unit>? = null

    override suspend fun getProducts(limit: Int, skip: Int) = page("all:$skip", products, limit, skip)

    override suspend fun searchProducts(query: String, limit: Int, skip: Int) =
        page("search:$query:$skip", products.filter { it.title.contains(query, ignoreCase = true) }, limit, skip)

    override suspend fun getProductsByCategory(slug: String, limit: Int, skip: Int) =
        page("category:$slug:$skip", products.filter { it.category == slug }, limit, skip)

    override suspend fun getProduct(id: Int): DataState<Product> {
        calls += "product:$id"
        if (consumeFailure()) return DataState.Error(ErrorType.NETWORK)
        return products.find { it.id == id }?.let { DataState.Success(it) } ?: DataState.Error(ErrorType.NOT_FOUND)
    }

    override suspend fun getCategories(): DataState<List<Category>> {
        calls += "categories"
        return categories
    }

    private suspend fun page(call: String, list: List<Product>, limit: Int, skip: Int): DataState<ProductsResponse> {
        calls += call
        if (skip > 0) nextPageGate?.await() else firstPageGate?.await()
        if (consumeFailure()) return DataState.Error(ErrorType.NETWORK)
        return DataState.Success(ProductsResponse(list.drop(skip).take(limit), list.size, skip, limit))
    }

    private fun consumeFailure(): Boolean = failNextProductCall.also { failNextProductCall = false }
}
