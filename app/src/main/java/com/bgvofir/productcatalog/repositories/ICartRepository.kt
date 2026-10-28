package com.bgvofir.productcatalog.repositories

import com.bgvofir.productcatalog.data.CartItem
import com.bgvofir.productcatalog.data.api.Product
import kotlinx.coroutines.flow.StateFlow

// Single source of truth for the cart. Every screen observes `items`, so a change made on one
// screen shows up on the others immediately.
interface ICartRepository {
    val items: StateFlow<List<CartItem>>

    // Adds one unit. Returns false if the product is out of stock or already at its stock limit.
    fun add(product: Product): Boolean

    fun increment(productId: Int)

    // Removes the item when its quantity would drop below 1.
    fun decrement(productId: Int)

    fun remove(productId: Int)

    fun clear()
}
