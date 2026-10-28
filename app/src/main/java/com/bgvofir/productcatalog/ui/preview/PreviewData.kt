package com.bgvofir.productcatalog.ui.preview

import com.bgvofir.productcatalog.data.CartItem
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.data.api.Product

// Sample data for Compose previews only. Images don't load in previews, so cards show the
// image placeholder color.
internal object PreviewData {
    val mascara = Product(
        id = 1,
        title = "Essence Mascara Lash Princess",
        description = "A popular mascara known for its volumizing and lengthening effects.",
        category = "beauty",
        price = 9.99,
        discountPercentage = 10.48,
        rating = 2.56,
        stock = 99,
        brand = "Essence",
        thumbnail = "",
    )

    val apple = Product(
        id = 16,
        title = "Apple",
        description = "Fresh and crisp apples, perfect for snacking.",
        category = "groceries",
        price = 1.99,
        discountPercentage = 0.0,
        rating = 4.19,
        stock = 8,
        thumbnail = "",
    )

    val outOfStockPhone = Product(
        id = 132,
        title = "Samsung Galaxy S8",
        description = "A premium smartphone with an Infinity Display.",
        category = "smartphones",
        price = 499.99,
        discountPercentage = 19.45,
        rating = 4.4,
        stock = 0,
        brand = "Samsung",
        thumbnail = "",
    )

    val products = listOf(mascara, apple, outOfStockPhone, mascara.copy(id = 2, title = "Eyeshadow Palette with Mirror"))

    val categories = listOf(Category("beauty", "Beauty"), Category("groceries", "Groceries"), Category("smartphones", "Smartphones"))

    val cartItems = listOf(CartItem(mascara, 2), CartItem(apple, 1))
}
