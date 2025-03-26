package com.example.arfurnitureapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.arfurnitureapp.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SortOrder {
    NONE,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}

data class FilterState(
    val minPrice: Double = 0.0,
    val maxPrice: Double = 1000.0,
    val sortOrder: SortOrder = SortOrder.NONE,
    val showOnlyInStock: Boolean = false
)

class FilterViewModel : ViewModel() {
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    fun updatePriceRange(min: Double, max: Double) {
        _filterState.update { it.copy(minPrice = min, maxPrice = max) }
    }

    fun updateSortOrder(order: SortOrder) {
        _filterState.update { it.copy(sortOrder = order) }
    }

    fun toggleInStockFilter(showOnlyInStock: Boolean) {
        _filterState.update { it.copy(showOnlyInStock = showOnlyInStock) }
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun applyFilters(products: List<Product>): List<Product> {
        val currentState = _filterState.value

        // Filter by price range
        var filteredProducts = products.filter {
            it.price >= currentState.minPrice && it.price <= currentState.maxPrice
        }

        // Sort by selected order
        filteredProducts = when (currentState.sortOrder) {
            SortOrder.PRICE_LOW_TO_HIGH -> filteredProducts.sortedBy { it.price }
            SortOrder.PRICE_HIGH_TO_LOW -> filteredProducts.sortedByDescending { it.price }
            SortOrder.NONE -> filteredProducts
        }

        return filteredProducts
    }
}