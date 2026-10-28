package com.bgvofir.productcatalog.data.api

import kotlinx.serialization.Serializable

// Models mirror the dummyjson JSON. Fields the app does not use are ignored by the Json config.
// One Product class is reused by the list, detail and cart screens.

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    // Missing from the JSON for products without a brand.
    val brand: String? = null,
    val thumbnail: String,
    val images: List<String> = emptyList(),
) {
    val hasDiscount: Boolean
        get() = discountPercentage > 0

    // `price` is already discounted, so the original price is price / (1 - discount).
    // Null when there is no discount, or at 100% where the original price can't be recovered.
    val originalPrice: Double?
        get() = if (hasDiscount && discountPercentage < 100) price / (1 - discountPercentage / 100) else null

    val isInStock: Boolean
        get() = stock > 0
}

@Serializable
data class ProductsResponse(
    val products: List<Product>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class Category(
    val slug: String,
    val name: String,
)
