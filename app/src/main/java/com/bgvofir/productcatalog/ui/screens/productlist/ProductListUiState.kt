package com.bgvofir.productcatalog.ui.screens.productlist

import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.data.api.Product

data class ProductListUiState(
    val query: String = "",
    // Category slug; null means "All".
    val selectedCategory: String? = null,
    val categories: List<Category> = emptyList(),
    val categoriesFailed: Boolean = false,
    // Loading / Error cover the whole list. Success with no products is the empty state.
    val content: DataState<ProductPage> = DataState.Loading,
    val cartCount: Int = 0,
)

// The products loaded so far, plus the state of loading the next page.
data class ProductPage(
    val products: List<Product>,
    val total: Int,
    val isLoadingMore: Boolean = false,
    val loadMoreFailed: Boolean = false,
) {
    val endReached: Boolean
        get() = products.size >= total
}
