package com.bgvofir.productcatalog.data

import com.bgvofir.productcatalog.data.api.Product

data class CartItem(
    val product: Product,
    val quantity: Int,
) {
    val lineTotal: Double
        get() = product.price * quantity
}

// Badge count: units in the cart, not distinct products.
val List<CartItem>.totalQuantity: Int
    get() = sumOf { it.quantity }

val List<CartItem>.totalPrice: Double
    get() = sumOf { it.lineTotal }
