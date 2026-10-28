package com.bgvofir.productcatalog.repositories

import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.data.api.Product
import com.bgvofir.productcatalog.data.api.ProductsResponse

// Returns Success or Error only; Loading is set by the caller before it calls.
interface IProductRepository {
    suspend fun getProducts(limit: Int, skip: Int): DataState<ProductsResponse>

    suspend fun searchProducts(query: String, limit: Int, skip: Int): DataState<ProductsResponse>

    suspend fun getProductsByCategory(slug: String, limit: Int, skip: Int): DataState<ProductsResponse>

    suspend fun getProduct(id: Int): DataState<Product>

    suspend fun getCategories(): DataState<List<Category>>
}
