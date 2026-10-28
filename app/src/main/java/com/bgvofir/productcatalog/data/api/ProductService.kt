package com.bgvofir.productcatalog.data.api

import kotlinx.serialization.json.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://dummyjson.com/"

// Shared by Retrofit and the parsing tests, so both use the same rules.
val ApiJson = Json { ignoreUnknownKeys = true }

interface ProductService {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
    ): ProductsResponse

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
    ): ProductsResponse

    @GET("products/category/{slug}")
    suspend fun getProductsByCategory(
        @Path("slug") slug: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
    ): ProductsResponse

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Product

    @GET("products/categories")
    suspend fun getCategories(): List<Category>
}
