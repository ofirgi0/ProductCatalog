package com.bgvofir.productcatalog.repositories

import com.bgvofir.productcatalog.data.CartItem
import com.bgvofir.productcatalog.data.api.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// In memory, as the spec allows. `update` is atomic, so concurrent changes are never lost.
// Scoped on the class too, so even injecting the concrete type never creates a second cart.
@Singleton
class CartRepository @Inject constructor() : ICartRepository {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    override val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    override fun add(product: Product): Boolean {
        var added = false
        _items.update { items ->
            val existing = items.find { it.product.id == product.id }
            val quantity = existing?.quantity ?: 0
            added = quantity < product.stock
            val newQuantity = if (added) quantity + 1 else quantity
            when {
                existing == null -> if (added) items + CartItem(product, 1) else items
                // Always keep the latest product data, even when the add is refused, so
                // increment() checks the same (current) stock.
                else -> items.map {
                    if (it.product.id == product.id) it.copy(product = product, quantity = newQuantity) else it
                }
            }
        }
        return added
    }

    override fun increment(productId: Int) {
        _items.update { items ->
            items.map {
                if (it.product.id == productId && it.quantity < it.product.stock) it.copy(quantity = it.quantity + 1) else it
            }
        }
    }

    override fun decrement(productId: Int) {
        _items.update { items ->
            items.mapNotNull {
                when {
                    it.product.id != productId -> it
                    it.quantity > 1 -> it.copy(quantity = it.quantity - 1)
                    else -> null
                }
            }
        }
    }

    override fun remove(productId: Int) {
        _items.update { items -> items.filterNot { it.product.id == productId } }
    }

    override fun clear() {
        _items.value = emptyList()
    }
}
