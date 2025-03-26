package com.example.arfurnitureapp.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val favorites: List<String> = emptyList()  // List of product IDs
)