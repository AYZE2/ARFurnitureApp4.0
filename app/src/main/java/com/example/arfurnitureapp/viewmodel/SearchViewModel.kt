package com.example.arfurnitureapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.arfurnitureapp.data.SampleData
import com.example.arfurnitureapp.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel : ViewModel() {
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All products
    private val allProducts = SampleData.getPopularProducts() + SampleData.getRecentlyViewedProducts()

    // Filtered products based on search
    private val _searchResults = MutableStateFlow<List<Product>>(allProducts)
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    // Update search query and filter products
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        // Filter products based on search query
        if (query.isEmpty()) {
            _searchResults.value = allProducts
        } else {
            _searchResults.value = allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
            }
        }
    }

    // Perform search (for explicit search action)
    fun search(query: String) {
        updateSearchQuery(query)
    }
}
