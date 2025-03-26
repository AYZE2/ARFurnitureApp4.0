package com.example.arfurnitureapp.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val categoryId: String,
    val imageResId: Int,
    val modelResId: Int? = null // For 3D models in AR
)