package com.bgvofir.productcatalog.ui.screens.productdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bgvofir.productcatalog.R
import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.data.api.Product
import com.bgvofir.productcatalog.ui.components.DiscountTag
import com.bgvofir.productcatalog.ui.components.ErrorState
import com.bgvofir.productcatalog.ui.components.LoadingState
import com.bgvofir.productcatalog.ui.components.Rating
import com.bgvofir.productcatalog.ui.components.formatPrice

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductDetailContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onAddToCart = viewModel::addToCart,
        onMessageShown = viewModel::onMessageShown,
        onCartClick = onCartClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAddToCart: () -> Unit,
    onMessageShown: (Long) -> Unit,
    onCartClick: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val product = (state.product as? DataState.Success)?.data

    CartMessageSnackbar(
        message = state.message,
        stock = product?.stock ?: 0,
        snackbarHostState = snackbarHostState,
        onMessageShown = onMessageShown,
        onViewCart = onCartClick,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product?.title.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        bottomBar = {
            if (product != null) AddToCartBar(inStock = product.isInStock, onAddToCart = onAddToCart)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (val content = state.product) {
            DataState.Loading -> LoadingState(modifier)
            is DataState.Error -> ErrorState(type = content.type, onRetry = onRetry, modifier = modifier)
            is DataState.Success -> ProductDetails(product = content.data, modifier = modifier)
        }
    }
}

// Shows each new cart message once. A new message restarts the effect, which replaces the
// snackbar still on screen instead of queueing behind it.
@Composable
private fun CartMessageSnackbar(
    message: CartMessage?,
    stock: Int,
    snackbarHostState: SnackbarHostState,
    onMessageShown: (Long) -> Unit,
    onViewCart: () -> Unit,
) {
    val resources = LocalResources.current
    val currentOnMessageShown by rememberUpdatedState(onMessageShown)
    val currentOnViewCart by rememberUpdatedState(onViewCart)
    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        val text = when (message.type) {
            CartMessage.Type.ADDED -> resources.getString(R.string.added_to_cart)
            CartMessage.Type.LIMIT_REACHED -> resources.getQuantityString(R.plurals.cart_limit_reached, stock, stock)
        }
        val result = snackbarHostState.showSnackbar(
            message = text,
            actionLabel = resources.getString(R.string.view_cart),
            duration = SnackbarDuration.Short,
        )
        currentOnMessageShown(message.id)
        if (result == SnackbarResult.ActionPerformed) currentOnViewCart()
    }
}

@Composable
private fun AddToCartBar(inStock: Boolean, onAddToCart: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Button(
            onClick = onAddToCart,
            enabled = inStock,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .height(56.dp),
        ) {
            Text(stringResource(if (inStock) R.string.add_to_cart else R.string.out_of_stock))
        }
    }
}

@Composable
private fun ProductDetails(product: Product, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        AsyncImage(
            model = product.images.firstOrNull() ?: product.thumbnail,
            contentDescription = product.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CategoryLabel(product.category)
            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Rating(product.rating)
                StockIndicator(product.stock)
            }
            PriceRow(product)
            product.brand?.let { brand ->
                Text(
                    text = stringResource(R.string.brand, brand),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider()
            Text(
                text = stringResource(R.string.description_heading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CategoryLabel(slug: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = categoryName(slug),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StockIndicator(stock: Int) {
    if (stock > 0) {
        Text(
            text = stringResource(R.string.in_stock, stock),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Text(
            text = stringResource(R.string.out_of_stock),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun PriceRow(product: Product) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = formatPrice(product.price),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        product.originalPrice?.let { original ->
            Text(
                text = formatPrice(original),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
        }
        if (product.hasDiscount) DiscountTag(product.discountPercentage)
    }
}

// The API gives slugs like "home-decoration"; show them as "Home Decoration".
private fun categoryName(slug: String): String =
    slug.split('-').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
