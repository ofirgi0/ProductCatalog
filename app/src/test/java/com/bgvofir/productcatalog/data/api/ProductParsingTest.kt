package com.bgvofir.productcatalog.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductParsingTest {

    private fun readResource(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource(name)) { "missing $name" }.readText()

    @Test
    fun `parses a real API response and ignores unknown fields`() {
        val response = ApiJson.decodeFromString<ProductsResponse>(readResource("products_response.json"))

        assertEquals(194, response.total)
        assertEquals(2, response.products.size)
        val mascara = response.products[0]
        assertEquals(1, mascara.id)
        assertEquals("Essence", mascara.brand)
        assertEquals(9.99, mascara.price, 0.0)
        assertEquals(1, mascara.images.size)
    }

    @Test
    fun `missing brand becomes null`() {
        val response = ApiJson.decodeFromString<ProductsResponse>(readResource("products_response.json"))

        assertNull(response.products[1].brand)
    }

    @Test
    fun `original price is derived from the discounted price`() {
        val product = sampleProduct(price = 90.0, discountPercentage = 10.0)

        assertTrue(product.hasDiscount)
        assertEquals(100.0, product.originalPrice!!, 0.0001)
    }

    @Test
    fun `no discount means no original price`() {
        val product = sampleProduct(price = 50.0, discountPercentage = 0.0)

        assertFalse(product.hasDiscount)
        assertNull(product.originalPrice)
    }

    @Test
    fun `a 100 percent discount still counts as a discount but has no original price`() {
        val product = sampleProduct(price = 0.0, discountPercentage = 100.0)

        assertTrue(product.hasDiscount)
        assertNull(product.originalPrice)
    }
}

fun sampleProduct(
    id: Int = 1,
    price: Double = 10.0,
    discountPercentage: Double = 0.0,
    stock: Int = 5,
) = Product(
    id = id,
    title = "Product $id",
    description = "Description",
    category = "beauty",
    price = price,
    discountPercentage = discountPercentage,
    rating = 4.5,
    stock = stock,
    thumbnail = "https://example.com/$id.png",
)
