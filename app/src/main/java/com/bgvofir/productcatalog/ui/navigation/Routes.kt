package com.bgvofir.productcatalog.ui.navigation

import kotlinx.serialization.Serializable

// Type-safe routes: Navigation Compose turns these classes into route strings for us.
@Serializable
data object ProductListRoute

@Serializable
data class ProductDetailRoute(val productId: Int) {
    companion object {
        // Navigation stores each field in the destination's SavedStateHandle under its name,
        // so this must match the property above.
        const val PRODUCT_ID_ARG = "productId"
    }
}

@Serializable
data object CartRoute
