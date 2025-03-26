package com.example.arfurnitureapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.AddressRepository
import com.example.arfurnitureapp.model.Address
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AddressViewModel"

class AddressViewModel : ViewModel() {
    private val addressRepository = AddressRepository()

    // List of user addresses
    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    // Selected address for checkout
    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress: StateFlow<Address?> = _selectedAddress.asStateFlow()

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
        loadAddresses()
    }

    /**
     * Load all addresses for the current user
     */
    fun loadAddresses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

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
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading addresses", e)
                _error.value = "Failed to load addresses"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new address
     */
    fun addAddress(
        fullName: String,
        phoneNumber: String,
        addressLine1: String,
        addressLine2: String,
        town: String,
        county: String,
        postcode: String,
        country: String = "United Kingdom",
        isDefault: Boolean,
        label: String
    ) {
        // Validate input
        if (fullName.isBlank() || phoneNumber.isBlank() || addressLine1.isBlank() ||
            town.isBlank() || postcode.isBlank()
        ) {
            _error.value = "Please fill in all required fields"
            return
        }

        // UK postcode validation
        if (!isValidUKPostcode(postcode)) {
            _error.value = "Please enter a valid UK postcode"
            return
        }

        val newAddress = Address(
            fullName = fullName,
            phoneNumber = phoneNumber,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            town = town,
            county = county,
            postcode = postcode.uppercase(), // UK postcodes are stored uppercase
            country = country,
            isDefault = isDefault,
            label = label
        )

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val addressId = addressRepository.addAddress(newAddress)
                if (addressId != null) {
                    _successMessage.value = "Address added successfully"
                    loadAddresses() // Reload addresses to get the updated list
                } else {
                    _error.value = "Failed to add address"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding address", e)
                _error.value = "Error adding address: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing address
     */
    fun updateAddress(
        addressId: String,
        fullName: String,
        phoneNumber: String,
        addressLine1: String,
        addressLine2: String,
        town: String,
        county: String,
        postcode: String,
        country: String = "United Kingdom",
        isDefault: Boolean,
        label: String
    ) {
        // Validate input
        if (addressId.isBlank() || fullName.isBlank() || addressLine1.isBlank() ||
            town.isBlank() || postcode.isBlank()
        ) {
            _error.value = "Please fill in all required fields"
            return
        }

        // UK postcode validation
        if (!isValidUKPostcode(postcode)) {
            _error.value = "Please enter a valid UK postcode"
            return
        }

        val updatedAddress = Address(
            id = addressId,
            fullName = fullName,
            phoneNumber = phoneNumber,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            town = town,
            county = county,
            postcode = postcode.uppercase(), // UK postcodes are stored uppercase
            country = country,
            isDefault = isDefault,
            label = label
        )

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val success = addressRepository.updateAddress(updatedAddress)
                if (success) {
                    _successMessage.value = "Address updated successfully"
                    loadAddresses() // Reload addresses to get the updated list
                } else {
                    _error.value = "Failed to update address"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating address", e)
                _error.value = "Error updating address: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete an address
     */
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val success = addressRepository.deleteAddress(addressId)
                if (success) {
                    _successMessage.value = "Address deleted successfully"
                    loadAddresses() // Reload addresses to get the updated list
                } else {
                    _error.value = "Failed to delete address"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting address", e)
                _error.value = "Error deleting address: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set an address as the default
     */
    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            try {
                val success = addressRepository.setDefaultAddress(addressId)
                if (success) {
                    _successMessage.value = "Default address updated"
                    loadAddresses() // Reload addresses to get the updated list
                } else {
                    _error.value = "Failed to update default address"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting default address", e)
                _error.value = "Error updating default address: ${e.message}"
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
     * Validate UK postcode format
     * Basic UK postcode validation - should handle most common formats
     */
    private fun isValidUKPostcode(postcode: String): Boolean {
        // Simplified UK postcode regex pattern
        // This covers most common UK postcode formats
        val ukPostcodePattern = "^[A-Z]{1,2}[0-9][A-Z0-9]?\\s?[0-9][A-Z]{2}$".toRegex(RegexOption.IGNORE_CASE)
        return ukPostcodePattern.matches(postcode)
    }
}