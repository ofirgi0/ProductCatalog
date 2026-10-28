package com.bgvofir.productcatalog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bgvofir.productcatalog.ui.screens.cart.CartScreen
import com.bgvofir.productcatalog.ui.screens.productdetail.ProductDetailScreen
import com.bgvofir.productcatalog.ui.screens.productlist.ProductListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Navigation events are ignored while a screen is animating in or out. This stops a quick
    // double tap from opening a screen twice or popping past the first screen.
    NavHost(navController = navController, startDestination = ProductListRoute) {
        composable<ProductListRoute> { entry ->
            ProductListScreen(
                onProductClick = { id ->
                    if (entry.isResumed()) navController.navigate(ProductDetailRoute(id))
                },
                onCartClick = {
                    if (entry.isResumed()) navController.navigate(CartRoute)
                },
            )
        }
        composable<ProductDetailRoute> { entry ->
            val route = entry.toRoute<ProductDetailRoute>()
            ProductDetailScreen(
                productId = route.productId,
                onBack = { if (entry.isResumed()) navController.popBackStack() },
            )
        }
        composable<CartRoute> { entry ->
            CartScreen(onBack = { if (entry.isResumed()) navController.popBackStack() })
        }
    }
}

private fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED
