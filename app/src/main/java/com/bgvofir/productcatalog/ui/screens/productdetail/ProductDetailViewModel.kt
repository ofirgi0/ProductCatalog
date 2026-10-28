package com.bgvofir.productcatalog.ui.screens.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgvofir.productcatalog.data.DataState
import com.bgvofir.productcatalog.repositories.ICartRepository
import com.bgvofir.productcatalog.repositories.IProductRepository
import com.bgvofir.productcatalog.ui.navigation.ProductDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: IProductRepository,
    private val cartRepository: ICartRepository,
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle[ProductDetailRoute.PRODUCT_ID_ARG])

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var nextMessageId = 0L

    init {
        load()
    }

    fun retry() = load()

    fun addToCart() {
        val product = (_uiState.value.product as? DataState.Success)?.data ?: return
        // The button is disabled when out of stock; this guard keeps the ViewModel safe on its own.
        if (!product.isInStock) return
        val added = cartRepository.add(product)
        val type = if (added) CartMessage.Type.ADDED else CartMessage.Type.LIMIT_REACHED
        _uiState.update { it.copy(message = CartMessage(type, nextMessageId++)) }
    }

    // Clears only the message that was shown, so a newer one that arrived meanwhile isn't lost.
    fun onMessageShown(id: Long) {
        _uiState.update { if (it.message?.id == id) it.copy(message = null) else it }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.update { it.copy(product = DataState.Loading) }
        loadJob = viewModelScope.launch {
            val result = productRepository.getProduct(productId)
            _uiState.update { it.copy(product = result) }
        }
    }
}
