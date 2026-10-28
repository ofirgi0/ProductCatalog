package com.bgvofir.productcatalog.ui.navigation

import kotlinx.serialization.Serializable

// Type-safe routes: Navigation Compose turns these classes into route strings for us.
@Serializable
data object ProductListRoute

@Serializable
data class ProductDetailRoute(val productId: Int)

@Serializable
data object CartRoute
