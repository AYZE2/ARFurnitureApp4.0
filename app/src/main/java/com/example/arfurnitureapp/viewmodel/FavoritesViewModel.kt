package com.example.arfurnitureapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.FirestoreRepository
import com.example.arfurnitureapp.model.Product
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "FavoritesViewModel"

class FavoritesViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestoreRepository = FirestoreRepository()

    // List of saved product IDs
    private val _savedProductIds = MutableStateFlow<Set<String>>(emptySet())
    val savedProductIds: StateFlow<Set<String>> = _savedProductIds.asStateFlow()

    // List of saved products
    private val _savedProducts = MutableStateFlow<List<Product>>(emptyList())
    val savedProducts: StateFlow<List<Product>> = _savedProducts.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Load user favorites when ViewModel is created
        loadUserFavorites()
    }

    private fun loadUserFavorites() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                firestoreRepository.getUserFavorites(currentUser.uid).collect { favoriteIds ->
                    Log.d(TAG, "Received favorites: $favoriteIds")
                    _savedProductIds.value = favoriteIds.toSet()

                    // Now fetch actual product details for each favorite ID
                    val productsList = mutableListOf<Product>()
                    for (productId in favoriteIds) {
                        firestoreRepository.getProductById(productId)?.let { product ->
                            productsList.add(product)
                        }
                    }
                    _savedProducts.value = productsList
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading favorites", e)
                _error.value = "Failed to load favorites: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Toggle favorite status for a product
    fun toggleFavorite(product: Product) {
        val currentUser = auth.currentUser ?: run {
            _error.value = "You must be logged in to save favorites"
            return
        }

        Log.d(TAG, "Toggling favorite for: ${product.id} - ${product.name}")

        val currentIds = _savedProductIds.value
        val isFavorite = currentIds.contains(product.id)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = if (isFavorite) {
                    // Remove from favorites
                    Log.d(TAG, "Removing from favorites")
                    firestoreRepository.removeFromFavorites(currentUser.uid, product.id)
                } else {
                    // Add to favorites
                    Log.d(TAG, "Adding to favorites")
                    firestoreRepository.addToFavorites(currentUser.uid, product.id)
                }

                if (success) {
                    // Update local state immediately for better UI responsiveness
                    if (isFavorite) {
                        _savedProductIds.value = currentIds.minus(product.id)
                        _savedProducts.value = _savedProducts.value.filter { it.id != product.id }
                    } else {
                        _savedProductIds.value = currentIds.plus(product.id)
                        _savedProducts.value = _savedProducts.value + product
                    }
                    Log.d(TAG, "Favorites updated successfully. New count: ${_savedProductIds.value.size}")
                } else {
                    _error.value = "Failed to update favorites"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
                _error.value = "Error updating favorites: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Check if a product is saved as favorite
    fun isFavorite(productId: String): Boolean {
        return _savedProductIds.value.contains(productId)
    }

    // Remove a product from favorites
    fun removeFromFavorites(productId: String) {
        val currentUser = auth.currentUser ?: run {
            _error.value = "You must be logged in to manage favorites"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = firestoreRepository.removeFromFavorites(currentUser.uid, productId)

                if (success) {
                    // Update local state immediately
                    _savedProductIds.value = _savedProductIds.value.minus(productId)
                    _savedProducts.value = _savedProducts.value.filter { it.id != productId }
                    Log.d(TAG, "Successfully removed from favorites")
                } else {
                    _error.value = "Failed to remove from favorites"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing from favorites", e)
                _error.value = "Error removing from favorites: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Refresh favorites list (can be called manually if needed)
    fun refreshFavorites() {
        loadUserFavorites()
    }

    // Clear any error messages
    fun clearError() {
        _error.value = null
    }
}