package com.bgvofir.productcatalog.ui.navigation

import androidx.compose.runtime.Composable
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

    NavHost(navController = navController, startDestination = ProductListRoute) {
        composable<ProductListRoute> {
            ProductListScreen(
                onProductClick = { id -> navController.navigate(ProductDetailRoute(id)) },
                onCartClick = { navController.navigate(CartRoute) },
            )
        }
        composable<ProductDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProductDetailRoute>()
            ProductDetailScreen(
                productId = route.productId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<CartRoute> {
            CartScreen(onBack = { navController.popBackStack() })
        }
    }
}
