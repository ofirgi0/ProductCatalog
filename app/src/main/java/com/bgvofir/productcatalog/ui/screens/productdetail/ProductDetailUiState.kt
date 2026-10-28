package com.bgvofir.productcatalog.ui.screens.productdetail

import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.api.Product

data class ProductDetailUiState(
    val product: DataState<Product> = DataState.Loading,
    // A message for the snackbar. The screen shows it, then calls onMessageShown() to clear it.
    val message: CartMessage? = null,
)

// `id` makes two identical messages in a row different, so each tap shows its own snackbar.
data class CartMessage(val type: Type, val id: Long) {
    enum class Type { ADDED, LIMIT_REACHED }
}
