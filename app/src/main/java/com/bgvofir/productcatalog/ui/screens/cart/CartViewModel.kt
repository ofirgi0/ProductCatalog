package com.bgvofir.productcatalog.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgvofir.productcatalog.repositories.ICartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// The cart repository is the single source of truth; this ViewModel only shapes it for the
// screen and forwards user actions, so every change shows up on the list badge at once.
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: ICartRepository,
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = cartRepository.items
        .map { it.toCartUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), cartRepository.items.value.toCartUiState())

    fun increment(productId: Int) = cartRepository.increment(productId)

    fun decrement(productId: Int) = cartRepository.decrement(productId)

    fun remove(productId: Int) = cartRepository.remove(productId)

    fun clearCart() = cartRepository.clear()

    // There is no real checkout; the screen confirms with a Toast and the cart is emptied.
    fun checkout() = cartRepository.clear()
}
