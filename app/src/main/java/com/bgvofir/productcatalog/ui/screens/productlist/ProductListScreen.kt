package com.bgvofir.productcatalog.ui.screens.productlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bgvofir.productcatalog.R
import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.api.Category
import com.bgvofir.productcatalog.ui.components.EmptyState
import com.bgvofir.productcatalog.ui.components.ErrorState
import com.bgvofir.productcatalog.ui.components.LoadingState
import com.bgvofir.productcatalog.ui.components.ProductCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

// Start loading the next page when the last visible item is this close to the end.
private const val LOAD_MORE_THRESHOLD = 6

// Like GridCells.Adaptive(160.dp), but never fewer than two columns. Plain Adaptive drops to one
// column on 360dp phones once the screen padding and gaps are taken out.
private object ProductGridCells : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val count = maxOf(2, (availableSize + spacing) / (160.dp.roundToPx() + spacing))
        val usable = availableSize - spacing * (count - 1)
        val cellSize = usable / count
        val remainder = usable % count
        return List(count) { index -> cellSize + if (index < remainder) 1 else 0 }
    }
}

@Composable
fun ProductListScreen(
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductListContent(
        state = state,
        searchQuery = viewModel.searchQuery,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onRetryCategories = viewModel::retryCategories,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onProductClick = onProductClick,
        onCartClick = onCartClick,
    )
}

// Stateless, so it can be previewed and reused with any state.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListContent(
    state: ProductListUiState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onRetryCategories: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_list_title)) },
                actions = { CartButton(count = state.cartCount, onClick = onCartClick) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            SearchField(
                query = searchQuery,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            CategoryChips(
                categories = state.categories,
                selected = state.selectedCategory,
                failed = state.categoriesFailed,
                onSelected = onCategorySelected,
                onRetry = onRetryCategories,
            )
            when (val content = state.content) {
                DataState.Loading -> LoadingState()
                is DataState.Error -> ErrorState(type = content.type, onRetry = onRetry)
                is DataState.Success -> if (content.data.products.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = stringResource(R.string.empty_products_title),
                        message = stringResource(R.string.empty_products_message),
                    )
                } else {
                    ProductGrid(
                        page = content.data,
                        onProductClick = onProductClick,
                        onLoadMore = onLoadMore,
                        onRetryLoadMore = onRetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun CartButton(count: Int, onClick: () -> Unit) {
    val description = pluralStringResource(R.plurals.cart_content_description, count, count)
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                // The badge hides when the cart is empty.
                if (count > 0) Badge { Text(if (count > 99) "99+" else count.toString()) }
            },
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = description)
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // Results already update while typing, so the keyboard's search key just closes it.
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

@Composable
private fun CategoryChips(
    categories: List<Category>,
    selected: String?,
    failed: Boolean,
    onSelected: (String?) -> Unit,
    onRetry: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.category_all)) },
            )
        }
        items(categories, key = { it.slug }) { category ->
            FilterChip(
                selected = selected == category.slug,
                onClick = { onSelected(category.slug) },
                label = { Text(category.name) },
            )
        }
        if (failed) {
            item(key = "retry") {
                AssistChip(
                    onClick = onRetry,
                    label = { Text(stringResource(R.string.retry_categories)) },
                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    page: ProductPage,
    onProductClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
) {
    val gridState = rememberLazyGridState()
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    // Asks for the next page when the user nears the end. The item count is part of the key, so
    // it fires again after a page arrives even if the user is still at the bottom.
    LaunchedEffect(gridState) {
        snapshotFlow {
            val info = gridState.layoutInfo
            // Before the first layout nothing is visible yet, which must not count as "at the end".
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index
            val nearEnd = lastVisible != null && lastVisible >= info.totalItemsCount - LOAD_MORE_THRESHOLD
            nearEnd to info.totalItemsCount
        }
            .distinctUntilChanged()
            .filter { (nearEnd, _) -> nearEnd }
            .collect { currentOnLoadMore() }
    }

    LazyVerticalGrid(
        columns = ProductGridCells,
        state = gridState,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(page.products, key = { it.id }) { product ->
            ProductCard(product = product, onClick = { onProductClick(product.id) })
        }
        if (page.isLoadingMore) {
            item(key = "loading_more", span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
        if (page.loadMoreFailed) {
            item(key = "load_more_failed", span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.load_more_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onRetryLoadMore) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}
