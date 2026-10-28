package com.bgvofir.productcatalog.ui.screens.productdetail

import androidx.lifecycle.SavedStateHandle
import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.ErrorType
import com.bgvofir.productcatalog.data.api.sampleProduct
import com.bgvofir.productcatalog.repositories.CartRepository
import com.bgvofir.productcatalog.testutil.FakeProductRepository
import com.bgvofir.productcatalog.testutil.MainDispatcherRule
import com.bgvofir.productcatalog.ui.navigation.ProductDetailRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private val cart = CartRepository()

    private fun createViewModel(productId: Int) =
        ProductDetailViewModel(SavedStateHandle(mapOf(ProductDetailRoute.PRODUCT_ID_ARG to productId)), repository, cart)

    @Test
    fun `loads the product from the route id`() = runTest {
        val viewModel = createViewModel(7)
        assertEquals(DataState.Loading, viewModel.uiState.value.product)

        advanceUntilIdle()

        assertEquals(7, (viewModel.uiState.value.product as DataState.Success).data.id)
        assertEquals(listOf("product:7"), repository.calls)
    }

    @Test
    fun `an unknown id shows NOT_FOUND`() = runTest {
        val viewModel = createViewModel(9999)
        advanceUntilIdle()

        assertEquals(DataState.Error(ErrorType.NOT_FOUND), viewModel.uiState.value.product)
    }

    @Test
    fun `a failed load can be retried`() = runTest {
        repository.failNextProductCall = true
        val viewModel = createViewModel(1)
        advanceUntilIdle()
        assertEquals(DataState.Error(ErrorType.NETWORK), viewModel.uiState.value.product)

        viewModel.retry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.product is DataState.Success)
    }

    @Test
    fun `add to cart adds the product and reports ADDED`() = runTest {
        val viewModel = createViewModel(1)
        advanceUntilIdle()

        viewModel.addToCart()

        assertEquals(1, cart.items.value.single().quantity)
        assertEquals(CartMessage.Type.ADDED, viewModel.uiState.value.message?.type)
    }

    @Test
    fun `adding again increments the same cart item`() = runTest {
        val viewModel = createViewModel(1)
        advanceUntilIdle()

        viewModel.addToCart()
        viewModel.addToCart()

        assertEquals(2, cart.items.value.single().quantity)
    }

    @Test
    fun `each add produces a distinct message so every tap shows a snackbar`() = runTest {
        val viewModel = createViewModel(1)
        advanceUntilIdle()

        viewModel.addToCart()
        val first = viewModel.uiState.value.message
        viewModel.addToCart()

        assertNotEquals(first, viewModel.uiState.value.message)
    }

    @Test
    fun `adding past the stock reports LIMIT_REACHED`() = runTest {
        repository.products = listOf(sampleProduct(id = 1, stock = 1))
        val viewModel = createViewModel(1)
        advanceUntilIdle()

        viewModel.addToCart()
        viewModel.addToCart()

        assertEquals(1, cart.items.value.single().quantity)
        assertEquals(CartMessage.Type.LIMIT_REACHED, viewModel.uiState.value.message?.type)
    }

    @Test
    fun `an out of stock product is never added`() = runTest {
        repository.products = listOf(sampleProduct(id = 1, stock = 0))
        val viewModel = createViewModel(1)
        advanceUntilIdle()

        viewModel.addToCart()

        assertTrue(cart.items.value.isEmpty())
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `a shown message is cleared`() = runTest {
        val viewModel = createViewModel(1)
        advanceUntilIdle()
        viewModel.addToCart()

        viewModel.onMessageShown(viewModel.uiState.value.message!!.id)

        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `finishing an older snackbar does not clear a newer message`() = runTest {
        val viewModel = createViewModel(1)
        advanceUntilIdle()
        viewModel.addToCart()
        val first = viewModel.uiState.value.message!!
        viewModel.addToCart()

        viewModel.onMessageShown(first.id)

        assertNotEquals(null, viewModel.uiState.value.message)
        assertNotEquals(first.id, viewModel.uiState.value.message!!.id)
    }

    @Test
    fun `add to cart does nothing before the product has loaded`() = runTest {
        val viewModel = createViewModel(1)

        viewModel.addToCart()

        assertTrue(cart.items.value.isEmpty())
        assertNull(viewModel.uiState.value.message)
    }
}
