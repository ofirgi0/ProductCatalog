package com.bgvofir.productcatalog.repositories

import app.cash.turbine.test
import com.bgvofir.productcatalog.data.api.sampleProduct
import com.bgvofir.productcatalog.data.totalPrice
import com.bgvofir.productcatalog.data.totalQuantity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartRepositoryTest {

    private val cart = CartRepository()

    @Test
    fun `adding a new product creates one item with quantity 1`() {
        assertTrue(cart.add(sampleProduct(id = 1)))

        assertEquals(1, cart.items.value.size)
        assertEquals(1, cart.items.value[0].quantity)
    }

    @Test
    fun `adding the same product again increments instead of duplicating`() {
        cart.add(sampleProduct(id = 1))
        cart.add(sampleProduct(id = 1))

        assertEquals(1, cart.items.value.size)
        assertEquals(2, cart.items.value[0].quantity)
    }

    @Test
    fun `an out of stock product is not added`() {
        assertFalse(cart.add(sampleProduct(id = 1, stock = 0)))

        assertTrue(cart.items.value.isEmpty())
    }

    @Test
    fun `quantity is capped at stock`() {
        val product = sampleProduct(id = 1, stock = 2)
        cart.add(product)
        cart.add(product)

        assertFalse(cart.add(product))
        cart.increment(1)

        assertEquals(2, cart.items.value[0].quantity)
    }

    @Test
    fun `re-adding a product keeps its latest stock for later increments`() {
        cart.add(sampleProduct(id = 1, stock = 1))
        cart.add(sampleProduct(id = 1, stock = 3))

        cart.increment(1)

        assertEquals(3, cart.items.value[0].quantity)
        assertEquals(3, cart.items.value[0].product.stock)
    }

    @Test
    fun `a refused add still refreshes stock, so increment cannot exceed the new stock`() {
        cart.add(sampleProduct(id = 1, stock = 5))

        assertFalse(cart.add(sampleProduct(id = 1, stock = 1)))
        cart.increment(1)

        assertEquals(1, cart.items.value[0].quantity)
        assertEquals(1, cart.items.value[0].product.stock)
    }

    @Test
    fun `stock dropping to zero blocks further increments`() {
        cart.add(sampleProduct(id = 1, stock = 5))
        cart.add(sampleProduct(id = 1, stock = 5))

        assertFalse(cart.add(sampleProduct(id = 1, stock = 0)))
        cart.increment(1)

        assertEquals(2, cart.items.value[0].quantity)
        assertEquals(0, cart.items.value[0].product.stock)
    }

    @Test
    fun `items emits every change to collectors`() = runTest {
        cart.items.test {
            assertEquals(emptyList<Int>(), awaitItem().map { it.quantity })
            cart.add(sampleProduct(id = 1))
            assertEquals(listOf(1), awaitItem().map { it.quantity })
            cart.increment(1)
            assertEquals(listOf(2), awaitItem().map { it.quantity })
            cart.decrement(1)
            assertEquals(listOf(1), awaitItem().map { it.quantity })
            cart.clear()
            assertEquals(emptyList<Int>(), awaitItem().map { it.quantity })
        }
    }

    @Test
    fun `decrement below 1 removes the item`() {
        cart.add(sampleProduct(id = 1))
        cart.add(sampleProduct(id = 2))

        cart.decrement(1)

        assertEquals(listOf(2), cart.items.value.map { it.product.id })
    }

    @Test
    fun `decrement above 1 lowers the quantity`() {
        cart.add(sampleProduct(id = 1))
        cart.increment(1)

        cart.decrement(1)

        assertEquals(1, cart.items.value[0].quantity)
    }

    @Test
    fun `remove and clear empty the cart`() {
        cart.add(sampleProduct(id = 1))
        cart.add(sampleProduct(id = 2))

        cart.remove(1)
        assertEquals(listOf(2), cart.items.value.map { it.product.id })

        cart.clear()
        assertTrue(cart.items.value.isEmpty())
    }

    @Test
    fun `totals sum quantities and line prices`() {
        cart.add(sampleProduct(id = 1, price = 2.5))
        cart.increment(1)
        cart.add(sampleProduct(id = 2, price = 10.0))

        assertEquals(3, cart.items.value.totalQuantity)
        assertEquals(15.0, cart.items.value.totalPrice, 0.0001)
    }
}
