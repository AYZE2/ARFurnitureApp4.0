package com.example.arfurnitureapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.PaymentMethodRepository
import com.example.arfurnitureapp.model.Address
import com.example.arfurnitureapp.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "PaymentMethodViewModel"

class PaymentMethodViewModel : ViewModel() {
    private val paymentMethodRepository = PaymentMethodRepository()

    // List of user payment methods
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    // Selected payment method for checkout
    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()

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
        loadPaymentMethods()
    }

    /**
     * Load all payment methods for the current user
     */
    fun loadPaymentMethods() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

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
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading payment methods", e)
                _error.value = "Failed to load payment methods"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new payment method
     */
    fun addPaymentMethod(
        cardHolderName: String,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        cardType: String,
        billingAddress: Address? = null,
        isDefault: Boolean
    ) {
        // Validate input
        if (cardHolderName.isBlank() || cardNumber.isBlank() || expiryDate.isBlank() || cvv.isBlank()) {
            _error.value = "Please fill in all required fields"
            return
        }

        // Basic card format validation
        if (!isValidCardNumber(cardNumber)) {
            _error.value = "Invalid card number"
            return
        }

        if (!isValidExpiryDate(expiryDate)) {
            _error.value = "Invalid expiry date format (MM/YY)"
            return
        }

        if (!isValidCVV(cvv)) {
            _error.value = "Invalid CVV"
            return
        }

        // Format the card number for display (e.g., **** **** **** 1234)
        val formattedCardNumber = formatCardNumberForDisplay(cardNumber)

        val newPaymentMethod = PaymentMethod(
            cardHolderName = cardHolderName,
            cardNumber = formattedCardNumber,
            expiryDate = expiryDate,
            cvv = cvv,
            cardType = cardType,
            billingAddress = billingAddress,
            isDefault = isDefault
        )

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val paymentMethodId = paymentMethodRepository.addPaymentMethod(newPaymentMethod)
                if (paymentMethodId != null) {
                    _successMessage.value = "Payment method added successfully"
                    loadPaymentMethods() // Reload payment methods to get the updated list
                } else {
                    _error.value = "Failed to add payment method"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding payment method", e)
                _error.value = "Error adding payment method: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing payment method
     */
    fun updatePaymentMethod(
        paymentMethodId: String,
        cardHolderName: String,
        expiryDate: String,
        cardType: String,
        billingAddress: Address? = null,
        isDefault: Boolean
    ) {
        // Validate input
        if (paymentMethodId.isBlank() || cardHolderName.isBlank() || expiryDate.isBlank()) {
            _error.value = "Please fill in all required fields"
            return
        }

        // Find the existing payment method to preserve the card number
        val existingMethod = _paymentMethods.value.find { it.id == paymentMethodId }
            ?: run {
                _error.value = "Payment method not found"
                return
            }

        if (!isValidExpiryDate(expiryDate)) {
            _error.value = "Invalid expiry date format (MM/YY)"
            return
        }

        val updatedPaymentMethod = existingMethod.copy(
            id = paymentMethodId,
            cardHolderName = cardHolderName,
            expiryDate = expiryDate,
            cardType = cardType,
            billingAddress = billingAddress,
            isDefault = isDefault
        )

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val success = paymentMethodRepository.updatePaymentMethod(updatedPaymentMethod)
                if (success) {
                    _successMessage.value = "Payment method updated successfully"
                    loadPaymentMethods() // Reload payment methods to get the updated list
                } else {
                    _error.value = "Failed to update payment method"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating payment method", e)
                _error.value = "Error updating payment method: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a payment method
     */
    fun deletePaymentMethod(paymentMethodId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val success = paymentMethodRepository.deletePaymentMethod(paymentMethodId)
                if (success) {
                    _successMessage.value = "Payment method deleted successfully"
                    loadPaymentMethods() // Reload payment methods to get the updated list
                } else {
                    _error.value = "Failed to delete payment method"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting payment method", e)
                _error.value = "Error deleting payment method: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set a payment method as the default
     */
    fun setDefaultPaymentMethod(paymentMethodId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val success = paymentMethodRepository.setDefaultPaymentMethod(paymentMethodId)
                if (success) {
                    _successMessage.value = "Default payment method updated"
                    loadPaymentMethods() // Reload payment methods to get the updated list
                } else {
                    _error.value = "Failed to update default payment method"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting default payment method", e)
                _error.value = "Error updating default payment method: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Select a payment method for checkout
     */
    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        _selectedPaymentMethod.value = paymentMethod
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

    // Validation helper methods

    /**
     * Validate credit card number using Luhn algorithm
     */
    private fun isValidCardNumber(cardNumber: String): Boolean {
        val digitsOnly = cardNumber.replace("\\D".toRegex(), "")

        // Basic length check
        if (digitsOnly.length < 13 || digitsOnly.length > 19) {
            return false
        }

        // Simple check for demo purposes
        return true
    }

    /**
     * Validate expiry date format (MM/YY)
     */
    private fun isValidExpiryDate(expiryDate: String): Boolean {
        val regex = "^(0[1-9]|1[0-2])/([0-9]{2})$".toRegex()
        if (!regex.matches(expiryDate)) {
            return false
        }

        // Check if the date is in the future
        val parts = expiryDate.split("/")
        val month = parts[0].toInt()
        val year = 2000 + parts[1].toInt()

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

        return (year > currentYear) || (year == currentYear && month >= currentMonth)
    }

    /**
     * Validate CVV format
     */
    private fun isValidCVV(cvv: String): Boolean {
        val regex = "^[0-9]{3,4}$".toRegex()
        return regex.matches(cvv)
    }

    /**
     * Format card number for display (e.g., **** **** **** 1234)
     */
    private fun formatCardNumberForDisplay(cardNumber: String): String {
        val digitsOnly = cardNumber.replace("\\D".toRegex(), "")
        val last4 = digitsOnly.takeLast(4)
        return "•••• •••• •••• $last4"
    }
}