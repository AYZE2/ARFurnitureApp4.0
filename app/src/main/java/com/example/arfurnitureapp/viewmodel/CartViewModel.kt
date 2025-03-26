package com.example.arfurnitureapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.FirestoreRepository
import com.example.arfurnitureapp.data.repositories.RealtimeDatabaseRepository
import com.example.arfurnitureapp.model.Product
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "CartViewModel"

data class CartItem(
    val product: Product,
    val quantity: Int = 1
)

class CartViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val realtimeDbRepository = RealtimeDatabaseRepository()
    private val firestoreRepository = FirestoreRepository()

    // Cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Total price
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Load user cart when ViewModel is created
        loadUserCart()
    }

    private fun loadUserCart() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                firestoreRepository.getUserCart(currentUser.uid).collect { cartMap ->
                    val cartItemsList = mutableListOf<CartItem>()

                    // Now fetch actual product details for each cart item
                    for ((productId, quantity) in cartMap) {
                        firestoreRepository.getProductById(productId)?.let { product ->
                            cartItemsList.add(CartItem(product, quantity))
                        }
                    }

                    _cartItems.value = cartItemsList
                    updateTotalPrice()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cart", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add product to cart
    fun addToCart(product: Product) {
        val currentUser = auth.currentUser ?: return
        Log.d(TAG, "Adding to cart: ${product.id} - ${product.name}")

        val existingItem = _cartItems.value.find { it.product.id == product.id }
        val newQuantity = (existingItem?.quantity ?: 0) + 1

        viewModelScope.launch {
            val success = realtimeDbRepository.addToCart(currentUser.uid, product.id, newQuantity)
            if (success) {
                // The cart flow will update automatically via the collector
                Log.d(TAG, "Successfully added to cart")
            }
        }
    }

    // Remove item from cart
    fun removeFromCart(productId: String) {
        val currentUser = auth.currentUser ?: return
        Log.d(TAG, "Removing from cart: $productId")

        viewModelScope.launch {
            val success = realtimeDbRepository.removeFromCart(currentUser.uid, productId)
            if (success) {
                // The cart flow will update automatically via the collector
                Log.d(TAG, "Successfully removed from cart")
            }
        }
    }

    // Update item quantity
    fun updateQuantity(productId: String, quantity: Int) {
        val currentUser = auth.currentUser ?: return
        Log.d(TAG, "Updating quantity for $productId to $quantity")

        viewModelScope.launch {
            val success = realtimeDbRepository.updateCartItemQuantity(currentUser.uid, productId, quantity)
            if (success) {
                // The cart flow will update automatically via the collector
                Log.d(TAG, "Successfully updated quantity")
            }
        }
    }

    // Clear cart
    fun clearCart() {
        val currentUser = auth.currentUser ?: return
        Log.d(TAG, "Clearing cart")

        viewModelScope.launch {
            val success = realtimeDbRepository.clearCart(currentUser.uid)
            if (success) {
                // The cart flow will update automatically via the collector
                Log.d(TAG, "Successfully cleared cart")
            }
        }
    }

    // Get cart size
    fun getCartSize(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    // Calculate and update total price
    private fun updateTotalPrice() {
        _totalPrice.value = _cartItems.value.sumOf {
            it.product.price * it.quantity
        }
    }

    // Refresh cart (can be called manually if needed)
    fun refreshCart() {
        loadUserCart()
    }
}