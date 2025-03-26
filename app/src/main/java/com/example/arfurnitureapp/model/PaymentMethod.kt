package com.example.arfurnitureapp.model

data class PaymentMethod(
    val id: String = "",
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val cardType: String = "Visa", // Visa, Mastercard, etc.
    val billingAddress: Address? = null,
    val isDefault: Boolean = false
)
