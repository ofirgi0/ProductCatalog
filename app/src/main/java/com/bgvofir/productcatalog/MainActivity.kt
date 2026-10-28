package com.bgvofir.productcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bgvofir.productcatalog.ui.navigation.AppNavigation
import com.bgvofir.productcatalog.ui.theme.ProductCatalogTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The default styles follow the system dark mode, like ProductCatalogTheme does, so the
        // status and navigation bar icons always contrast with the app.
        enableEdgeToEdge()
        setContent {
            ProductCatalogTheme {
                AppNavigation()
            }
        }
    }
}
