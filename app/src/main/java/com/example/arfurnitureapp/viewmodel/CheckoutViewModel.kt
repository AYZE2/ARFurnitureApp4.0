package com.example.arfurnitureapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.AddressRepository
import com.example.arfurnitureapp.data.repositories.OrderRepository
import com.example.arfurnitureapp.data.repositories.PaymentMethodRepository
import com.example.arfurnitureapp.model.Address
import com.example.arfurnitureapp.model.OrderSummary
import com.example.arfurnitureapp.model.PaymentMethod
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "CheckoutViewModel"

class CheckoutViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val addressRepository = AddressRepository()
    private val paymentMethodRepository = PaymentMethodRepository()
    private val orderRepository = OrderRepository()

    // Addresses
    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    // Selected address
    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress: StateFlow<Address?> = _selectedAddress.asStateFlow()

    // Payment methods
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    // Selected payment method
    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()

    // Checkout steps
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Order summary
    private val _orderSummary = MutableStateFlow<OrderSummary?>(null)
    val orderSummary: StateFlow<OrderSummary?> = _orderSummary.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        loadAddresses()
        loadPaymentMethods()
    }

    /**
     * Load user addresses from repository
     */
    private fun loadAddresses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                addressRepository.getUserAddresses().collect { userAddresses ->
                    _addresses.value = userAddresses

                    // Auto-select the default address if available
                    val defaultAddress = userAddresses.find { it.isDefault }
                    if (defaultAddress != null) {
                        _selectedAddress.value = defaultAddress
                    } else if (userAddresses.isNotEmpty()) {
                        _selectedAddress.value = userAddresses.first()
                    }

                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading addresses", e)
                _errorMessage.value = "Failed to load addresses: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user payment methods from repository
     */
    private fun loadPaymentMethods() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                paymentMethodRepository.getUserPaymentMethods().collect { userPaymentMethods ->
                    _paymentMethods.value = userPaymentMethods

                    // Auto-select the default payment method if available
                    val defaultPaymentMethod = userPaymentMethods.find { it.isDefault }
                    if (defaultPaymentMethod != null) {
                        _selectedPaymentMethod.value = defaultPaymentMethod
                    } else if (userPaymentMethods.isNotEmpty()) {
                        _selectedPaymentMethod.value = userPaymentMethods.first()
                    }

                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading payment methods", e)
                _errorMessage.value = "Failed to load payment methods: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new address during checkout
     */
    fun addAddress(address: Address) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val addressId = addressRepository.addAddress(address)
                if (addressId != null) {
                    // Reload addresses
                    loadAddresses()
                } else {
                    _errorMessage.value = "Failed to add address"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding address", e)
                _errorMessage.value = "Error adding address: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new payment method during checkout
     */
    fun addPaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val paymentMethodId = paymentMethodRepository.addPaymentMethod(paymentMethod)
                if (paymentMethodId != null) {
                    // Reload payment methods
                    loadPaymentMethods()
                } else {
                    _errorMessage.value = "Failed to add payment method"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding payment method", e)
                _errorMessage.value = "Error adding payment method: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Select an address for checkout
     */
    fun selectAddress(address: Address) {
        _selectedAddress.value = address
    }

    /**
     * Select a payment method for checkout
     */
    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        _selectedPaymentMethod.value = paymentMethod
    }

    /**
     * Place an order
     */
    fun placeOrder(cartItems: List<CartItem>, subtotal: Double): Boolean {
        val selectedAddress = _selectedAddress.value
        val selectedPayment = _selectedPaymentMethod.value

        if (selectedAddress == null) {
            _errorMessage.value = "Please select a shipping address"
            return false
        }

        if (selectedPayment == null) {
            _errorMessage.value = "Please select a payment method"
            return false
        }

        // Calculate totals
        val tax = subtotal * 0.08 // 8% tax
        val shipping = if (subtotal > 50) 0.0 else 9.99 // Free shipping over $50
        val total = subtotal + tax + shipping

        // Get the last 4 digits of the card
        val cardLast4 = selectedPayment.cardNumber.takeLast(4)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val orderId = orderRepository.createOrder(
                    cartItems = cartItems,
                    shippingAddress = selectedAddress,
                    billingAddress = selectedAddress, // Using same address for billing for simplicity
                    paymentMethodId = selectedPayment.id,
                    paymentMethodLast4 = cardLast4,
                    subtotal = subtotal,
                    tax = tax,
                    shipping = shipping,
                    discount = 0.0
                )

                if (orderId != null) {
                    // Create order summary for confirmation page
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    _orderSummary.value = OrderSummary(
                        orderId = orderId,
                        items = cartItems,
                        subtotal = subtotal,
                        tax = tax,
                        shipping = shipping,
                        total = total,
                        shippingAddress = selectedAddress,
                        paymentMethod = selectedPayment,
                        orderDate = currentDate,
                        orderStatus = "Processing"
                    )

                    _isLoading.value = false
                    return@launch
                } else {
                    _errorMessage.value = "Failed to place order"
                    _isLoading.value = false
                    return@launch
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error placing order", e)
                _errorMessage.value = "Error placing order: ${e.message}"
                _isLoading.value = false
                return@launch
            }
        }

        return true
    }

    /**
     * Go to next checkout step
     */
    fun nextStep() {
        _currentStep.value++
    }

    /**
     * Go to previous checkout step
     */
    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value--
        }
    }

    /**
     * Reset checkout
     */
    fun resetCheckout() {
        _currentStep.value = 1
        _orderSummary.value = null
        _errorMessage.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}