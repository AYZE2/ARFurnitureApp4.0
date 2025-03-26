package com.example.arfurnitureapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.ProductRepository
import com.example.arfurnitureapp.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Use getAllProducts() instead of getProducts()
                productRepository.getAllProducts().collect { productsList ->
                    _products.value = productsList
                }
            } catch (e: Exception) {
                // Handle error
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add method to fetch products by category
    fun fetchProductsByCategory(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.getProductsByCategory(categoryId).collect { productsList ->
                    _products.value = productsList
                }
            } catch (e: Exception) {
                // Handle error
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add method to search products
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = productRepository.searchProducts(query)
                _products.value = results
            } catch (e: Exception) {
                // Handle error
                _products.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Refresh products
    fun refreshProducts() {
        fetchProducts()
    }
}