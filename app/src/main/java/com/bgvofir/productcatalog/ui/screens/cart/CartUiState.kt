package com.bgvofir.productcatalog.ui.screens.cart

import com.bgvofir.productcatalog.data.CartItem
import com.bgvofir.productcatalog.data.totalPrice
import com.bgvofir.productcatalog.data.totalQuantity

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val totalQuantity: Int = 0,
)

fun List<CartItem>.toCartUiState() = CartUiState(this, totalPrice, totalQuantity)
