package com.bgvofir.productcatalog.ui.screens.productlist

import androidx.lifecycle.SavedStateHandle
import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.ErrorType
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.data.api.sampleProduct
import com.bgvofir.productcatalog.repositories.CartRepository
import com.bgvofir.productcatalog.testutil.FakeProductRepository
import com.bgvofir.productcatalog.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()
    private val cart = CartRepository()

    // uiState only runs while someone collects it (like the screen does), so tests collect too.
    private fun TestScope.createViewModel(savedState: SavedStateHandle = SavedStateHandle()): ProductListViewModel {
        val viewModel = ProductListViewModel(repository, cart, savedState)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    private val ProductListViewModel.page: ProductPage
        get() = (uiState.value.content as DataState.Success).data

    @Test
    fun `first page and categories load on start`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(20, viewModel.page.products.size)
        assertEquals(45, viewModel.page.total)
        assertEquals(listOf(Category("beauty", "Beauty")), viewModel.uiState.value.categories)
        assertTrue("all:0" in repository.calls)
    }

    @Test
    fun `an empty result is Success with no products`() = runTest {
        repository.products = emptyList()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.page.products.isEmpty())
    }

    @Test
    fun `a failed first page shows an error and retry reloads it`() = runTest {
        repository.failNextProductCall = true
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(DataState.Error(ErrorType.NETWORK), viewModel.uiState.value.content)

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(20, viewModel.page.products.size)
    }

    @Test
    fun `search waits 300ms after the last keystroke`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.calls.clear()

        viewModel.onQueryChange("Pro")
        advanceTimeBy(150)
        viewModel.onQueryChange("Product 1")
        advanceTimeBy(299)
        runCurrent()
        assertTrue(repository.calls.none { it.startsWith("search") })

        advanceTimeBy(2)
        runCurrent()
        assertEquals(listOf("search:Product 1:0"), repository.calls)
        // The text field shows the query immediately, before the debounce.
        assertEquals("Product 1", viewModel.uiState.value.query)
    }

    @Test
    fun `picking a category clears the query and loads without waiting`() = runTest {
        repository.products = (1..5).map { sampleProduct(id = it) }
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onQueryChange("Product")
        advanceUntilIdle()
        repository.calls.clear()

        viewModel.onCategorySelected("beauty")
        runCurrent()

        assertEquals("", viewModel.uiState.value.query)
        assertEquals("beauty", viewModel.uiState.value.selectedCategory)
        assertEquals(listOf("category:beauty:0"), repository.calls)
    }

    @Test
    fun `typing a query resets the category to All`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCategorySelected("beauty")
        advanceUntilIdle()
        repository.calls.clear()

        viewModel.onQueryChange("Product 2")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedCategory)
        // Only the search runs: no stray "All" request while the query is debounced.
        assertEquals(listOf("search:Product 2:0"), repository.calls)
    }

    @Test
    fun `load more appends pages and stops at the end`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(40, viewModel.page.products.size)

        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(45, viewModel.page.products.size)
        assertTrue(viewModel.page.endReached)

        repository.calls.clear()
        viewModel.loadMore()
        advanceUntilIdle()
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `a failed next page keeps the list and retry loads it`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.failNextProductCall = true
        viewModel.loadMore()
        advanceUntilIdle()
        assertTrue(viewModel.page.loadMoreFailed)
        assertEquals(20, viewModel.page.products.size)

        viewModel.retry()
        advanceUntilIdle()
        assertFalse(viewModel.page.loadMoreFailed)
        assertEquals(40, viewModel.page.products.size)
    }

    @Test
    fun `a new filter cancels a next page that is still loading`() = runTest {
        // 45 "beauty" products plus 25 "furniture" ones, so the two filters are distinguishable.
        repository.products = (1..45).map { sampleProduct(id = it) } +
            (46..70).map { sampleProduct(id = it).copy(category = "furniture") }
        val gate = CompletableDeferred<Unit>()
        repository.nextPageGate = gate
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        runCurrent()
        assertTrue(viewModel.page.isLoadingMore)

        viewModel.onCategorySelected("furniture")
        advanceUntilIdle()
        // The old request finishes after the filter changed; its page must not be appended.
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals((46..65).toList(), viewModel.page.products.map { it.id })
        assertFalse(viewModel.page.isLoadingMore)
    }

    @Test
    fun `search results are paged with the active query`() = runTest {
        repository.products = (1..30).map { sampleProduct(id = it).copy(title = "Phone $it") }
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onQueryChange("Phone")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertTrue("search:Phone:20" in repository.calls)
        assertEquals(30, viewModel.page.products.size)
    }

    @Test
    fun `category results are paged with the active category`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCategorySelected("beauty")
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertTrue("category:beauty:20" in repository.calls)
        assertEquals(40, viewModel.page.products.size)
    }

    @Test
    fun `typing and deleting within the debounce does not reload the list`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.calls.clear()

        viewModel.onQueryChange("a")
        advanceTimeBy(100)
        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertTrue(repository.calls.isEmpty())
        assertEquals(20, viewModel.page.products.size)
    }

    @Test
    fun `failed categories are reported and can be retried`() = runTest {
        repository.categories = DataState.Error(ErrorType.NETWORK)
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.categoriesFailed)
        // The products still load.
        assertEquals(20, viewModel.page.products.size)

        repository.categories = DataState.Success(listOf(Category("beauty", "Beauty")))
        viewModel.retryCategories()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.categoriesFailed)
        assertEquals(1, viewModel.uiState.value.categories.size)
    }

    @Test
    fun `the search field query updates synchronously`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("ph")

        assertEquals("ph", viewModel.searchQuery)
        viewModel.onCategorySelected("beauty")
        assertEquals("", viewModel.searchQuery)
    }

    @Test
    fun `cart badge count follows the shared cart`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        cart.add(sampleProduct(id = 1))
        cart.add(sampleProduct(id = 1))
        cart.add(sampleProduct(id = 2))
        runCurrent()

        assertEquals(3, viewModel.uiState.value.cartCount)
    }

    @Test
    fun `query and category are restored from saved state`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf("query" to "Product 3")))
        advanceUntilIdle()

        assertEquals("Product 3", viewModel.uiState.value.query)
        assertTrue("search:Product 3:0" in repository.calls)

        val restoredCategory = createViewModel(SavedStateHandle(mapOf("category" to "beauty")))
        advanceUntilIdle()
        assertEquals("beauty", restoredCategory.uiState.value.selectedCategory)
        assertTrue("category:beauty:0" in repository.calls)
    }
}
