package com.example.arfurnitureapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.OrderRepository
import com.example.arfurnitureapp.model.Address
import com.example.arfurnitureapp.model.Order
import com.example.arfurnitureapp.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "OrderViewModel"

class OrderViewModel : ViewModel() {
    private val orderRepository = OrderRepository()

    // List of user orders
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Selected order details
    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Load all orders for the current user
     */
    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                orderRepository.getUserOrders().collect { userOrders ->
                    _orders.value = userOrders
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading orders", e)
                _error.value = "Failed to load orders"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get order details by ID
     */
    fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val order = orderRepository.getOrderById(orderId)
                if (order != null) {
                    _selectedOrder.value = order
                } else {
                    _error.value = "Order not found"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting order details", e)
                _error.value = "Failed to load order details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new order from cart
     */
    fun createOrder(
        cartItems: List<CartItem>,
        shippingAddress: Address,
        billingAddress: Address,
        paymentMethodId: String,
        paymentMethodLast4: String,
        subtotal: Double,
        tax: Double,
        shipping: Double,
        discount: Double = 0.0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val orderId = orderRepository.createOrder(
                    cartItems = cartItems,
                    shippingAddress = shippingAddress,
                    billingAddress = billingAddress,
                    paymentMethodId = paymentMethodId,
                    paymentMethodLast4 = paymentMethodLast4,
                    subtotal = subtotal,
                    tax = tax,
                    shipping = shipping,
                    discount = discount
                )

                if (orderId != null) {
                    _successMessage.value = "Order placed successfully"
                    // Get the created order details
                    getOrderDetails(orderId)
                } else {
                    _error.value = "Failed to place order"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating order", e)
                _error.value = "Error placing order: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cancel an order
     */
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val success = orderRepository.cancelOrder(orderId)
                if (success) {
                    _successMessage.value = "Order cancelled successfully"
                    // Refresh the order details and list
                    getOrderDetails(orderId)
                    loadOrders()
                } else {
                    _error.value = "Failed to cancel order"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling order", e)
                _error.value = "Error cancelling order: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get filtered orders by status
     */
    fun getFilteredOrders(status: OrderStatus? = null): List<Order> {
        return if (status == null) {
            _orders.value
        } else {
            _orders.value.filter { it.status == status }
        }
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear any success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Clear selected order
     */
    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }
}