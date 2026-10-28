package com.bgvofir.productcatalog.ui.screens.productlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.api.ProductsResponse
import com.bgvofir.productcatalog.data.totalQuantity
import com.bgvofir.productcatalog.repositories.ICartRepository
import com.bgvofir.productcatalog.repositories.IProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: IProductRepository,
    cartRepository: ICartRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Query and category are restored after process death; the products are simply reloaded.
    private val state = MutableStateFlow(
        ProductListUiState(
            query = savedStateHandle[KEY_QUERY] ?: "",
            selectedCategory = savedStateHandle[KEY_CATEGORY],
        ),
    )

    // The text field reads this Compose state instead of uiState.query: a TextField must get each
    // keystroke back synchronously, or fast typing and IME composition can lose characters.
    var searchQuery by mutableStateOf(state.value.query)
        private set

    val uiState: StateFlow<ProductListUiState> =
        combine(state, cartRepository.items) { state, items -> state.copy(cartCount = items.totalQuantity) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state.value)

    // The filter the current list belongs to. Paging always continues this one, even while a
    // newer query is still waiting out its debounce.
    private var currentFilter = Filter.from(state.value)

    // One job for the first page and the next pages, so a new filter cancels both.
    private var pageJob: Job? = null
    private var categoriesJob: Job? = null

    // The user reached the end of the list while a refresh was running. The grid won't ask again
    // if nothing changes on screen, so the next page is loaded once the refresh finishes.
    private var loadMoreAfterRefresh = false

    init {
        loadCategories()
        viewModelScope.launch {
            state.map(Filter::from)
                .distinctUntilChanged()
                // Typing waits for a pause; picking a chip or clearing the search loads at once.
                .debounce { if (it.query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
                // Typing "a" and deleting it within the debounce must not reload the same list.
                .distinctUntilChanged()
                .collect(::loadFirstPage)
        }
    }

    fun onQueryChange(query: String) {
        searchQuery = query
        // Search and category are mutually exclusive: typing resets the chip to "All".
        state.update { it.copy(query = query, selectedCategory = null) }
        saveFilter()
    }

    fun onCategorySelected(slug: String?) {
        searchQuery = ""
        state.update { it.copy(selectedCategory = slug, query = "") }
        saveFilter()
    }

    fun retryCategories() = loadCategories()

    fun loadMore() {
        if (state.value.isRefreshing) {
            loadMoreAfterRefresh = true
            return
        }
        // No page yet means the first page is still loading; isLoadingMore means a page is on its way.
        val page = (state.value.content as? DataState.Success)?.data ?: return
        if (page.isLoadingMore || page.loadMoreFailed || page.endReached) return

        updatePage { copy(isLoadingMore = true) }
        pageJob = viewModelScope.launch {
            when (val result = fetch(currentFilter, skip = page.products.size)) {
                is DataState.Success -> updatePage {
                    val newProducts = result.data.products
                    copy(
                        products = (products + newProducts).distinctBy { it.id },
                        // An empty page means the API has nothing more, whatever `total` said.
                        total = if (newProducts.isEmpty()) products.size else result.data.total,
                        isLoadingMore = false,
                    )
                }
                is DataState.Error -> updatePage { copy(isLoadingMore = false, loadMoreFailed = true) }
                DataState.Loading -> Unit
            }
        }
    }

    // Retries whatever failed: the whole list, or just the next page.
    fun retry() {
        if (state.value.categories.isEmpty()) loadCategories()
        when (val content = state.value.content) {
            is DataState.Error -> loadFirstPage(currentFilter)
            is DataState.Success -> if (content.data.loadMoreFailed) {
                updatePage { copy(loadMoreFailed = false) }
                loadMore()
            }
            DataState.Loading -> Unit
        }
    }

    // Pull-to-refresh: reloads the first page of the current filter without the full-screen spinner.
    fun refresh() {
        if (state.value.categories.isEmpty()) loadCategories()
        pageJob?.cancel()
        state.update { it.copy(isRefreshing = true) }
        // A cancelled "load more" must not leave its footer spinner or retry row on screen.
        updatePage { copy(isLoadingMore = false, loadMoreFailed = false) }
        pageJob = viewModelScope.launch {
            val content = firstPage(currentFilter)
            state.update { it.copy(content = content, isRefreshing = false) }
            if (loadMoreAfterRefresh) {
                loadMoreAfterRefresh = false
                loadMore()
            }
        }
    }

    private fun loadFirstPage(filter: Filter) {
        currentFilter = filter
        pageJob?.cancel()
        if (state.value.categories.isEmpty()) loadCategories()
        // A new filter also ends a refresh that was still running (its job was just cancelled).
        loadMoreAfterRefresh = false
        state.update { it.copy(content = DataState.Loading, isRefreshing = false) }
        pageJob = viewModelScope.launch {
            val content = firstPage(filter)
            state.update { it.copy(content = content) }
        }
    }

    private suspend fun firstPage(filter: Filter): DataState<ProductPage> =
        when (val result = fetch(filter, skip = 0)) {
            is DataState.Success -> DataState.Success(ProductPage(result.data.products, result.data.total))
            is DataState.Error -> result
            DataState.Loading -> DataState.Loading
        }

    private fun loadCategories() {
        if (categoriesJob?.isActive == true) return
        state.update { it.copy(categoriesFailed = false) }
        categoriesJob = viewModelScope.launch {
            // A failure keeps the products on screen; the chip row offers its own retry.
            when (val result = productRepository.getCategories()) {
                is DataState.Success -> state.update { it.copy(categories = result.data) }
                is DataState.Error -> state.update { it.copy(categoriesFailed = true) }
                DataState.Loading -> Unit
            }
        }
    }

    private suspend fun fetch(filter: Filter, skip: Int): DataState<ProductsResponse> = when {
        filter.category != null -> productRepository.getProductsByCategory(filter.category, PAGE_SIZE, skip)
        filter.query.isNotEmpty() -> productRepository.searchProducts(filter.query, PAGE_SIZE, skip)
        else -> productRepository.getProducts(PAGE_SIZE, skip)
    }

    private fun updatePage(transform: ProductPage.() -> ProductPage) {
        state.update { current ->
            val content = current.content
            if (content is DataState.Success) current.copy(content = DataState.Success(content.data.transform())) else current
        }
    }

    private fun saveFilter() {
        savedStateHandle[KEY_QUERY] = state.value.query
        savedStateHandle[KEY_CATEGORY] = state.value.selectedCategory
    }

    // What decides which products are shown. A selected category ignores the query.
    private data class Filter(val query: String, val category: String?) {
        companion object {
            fun from(state: ProductListUiState) =
                if (state.selectedCategory != null) Filter("", state.selectedCategory) else Filter(state.query.trim(), null)
        }
    }

    companion object {
        const val PAGE_SIZE = 20
        const val SEARCH_DEBOUNCE_MS = 300L
        private const val KEY_QUERY = "query"
        private const val KEY_CATEGORY = "category"
    }
}
