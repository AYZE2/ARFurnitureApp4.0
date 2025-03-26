package com.example.arfurnitureapp.model

data class Address(
    val id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val addressLine1: String = "",         // House number/name and street
    val addressLine2: String = "",         // Optional: apartment, flat, etc.
    val town: String = "",                 // Town/City
    val county: String = "",               // County (optional in UK)
    val postcode: String = "",             // UK Postcode
    val country: String = "United Kingdom",
    val isDefault: Boolean = false,
    val label: String = "Home"             // Home, Work, etc.
)