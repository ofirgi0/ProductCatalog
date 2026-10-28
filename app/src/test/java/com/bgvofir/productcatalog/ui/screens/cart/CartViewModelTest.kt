package com.bgvofir.productcatalog.ui.screens.cart

import com.bgvofir.productcatalog.data.api.sampleProduct
import com.bgvofir.productcatalog.repositories.CartRepository
import com.bgvofir.productcatalog.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cart = CartRepository()

    private fun TestScope.createViewModel(): CartViewModel {
        val viewModel = CartViewModel(cart)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `starts with whatever is already in the shared cart`() = runTest {
        cart.add(sampleProduct(id = 1, price = 2.5))
        cart.add(sampleProduct(id = 1, price = 2.5))

        val viewModel = createViewModel()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(2, viewModel.uiState.value.totalQuantity)
        assertEquals(5.0, viewModel.uiState.value.totalPrice, 0.0001)
    }

    @Test
    fun `stepper changes update items and totals`() = runTest {
        cart.add(sampleProduct(id = 1, price = 10.0))
        cart.add(sampleProduct(id = 2, price = 1.0))
        val viewModel = createViewModel()

        viewModel.increment(1)
        runCurrent()
        assertEquals(21.0, viewModel.uiState.value.totalPrice, 0.0001)
        assertEquals(3, viewModel.uiState.value.totalQuantity)

        viewModel.decrement(1)
        runCurrent()
        assertEquals(11.0, viewModel.uiState.value.totalPrice, 0.0001)
    }

    @Test
    fun `decrementing below 1 removes the item`() = runTest {
        cart.add(sampleProduct(id = 1))
        val viewModel = createViewModel()

        viewModel.decrement(1)
        runCurrent()

        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    @Test
    fun `remove deletes only that item`() = runTest {
        cart.add(sampleProduct(id = 1))
        cart.add(sampleProduct(id = 2))
        val viewModel = createViewModel()

        viewModel.remove(1)
        runCurrent()

        assertEquals(listOf(2), viewModel.uiState.value.items.map { it.product.id })
    }

    @Test
    fun `clear cart and checkout both empty the shared cart`() = runTest {
        cart.add(sampleProduct(id = 1))
        val viewModel = createViewModel()

        viewModel.clearCart()
        runCurrent()
        assertTrue(viewModel.uiState.value.items.isEmpty())

        cart.add(sampleProduct(id = 2))
        viewModel.checkout()
        runCurrent()
        assertTrue(cart.items.value.isEmpty())
        assertEquals(0, viewModel.uiState.value.totalQuantity)
    }

    @Test
    fun `changes made elsewhere, like adding on the detail screen, show up here`() = runTest {
        val viewModel = createViewModel()

        cart.add(sampleProduct(id = 5, price = 3.0))
        runCurrent()

        assertEquals(1, viewModel.uiState.value.totalQuantity)
        assertEquals(3.0, viewModel.uiState.value.totalPrice, 0.0001)
    }
}
