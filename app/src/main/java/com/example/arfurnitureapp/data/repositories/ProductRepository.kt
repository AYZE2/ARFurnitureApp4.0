package com.example.arfurnitureapp.data.repositories

import android.util.Log
import com.example.arfurnitureapp.data.SampleData
import com.example.arfurnitureapp.model.Product
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ProductRepository {
    // Initialize Firebase Realtime Database with your specific URL
    private val database = FirebaseDatabase.getInstance("https://arfurniture-f8b1b-default-rtdb.europe-west1.firebasedatabase.app/")
    private val productsRef = database.getReference("products")
    private val realtimeDbRepository = RealtimeDatabaseRepository()

    // Get all products
    fun getAllProducts(): Flow<List<Product>> {
        return realtimeDbRepository.getProductsFlow()
    }

    // Get products by category
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        return realtimeDbRepository.getProductsByCategory(categoryId)
    }

    // Get product by ID
    suspend fun getProductById(productId: String): Product? {
        return realtimeDbRepository.getProductById(productId)
    }

    // Get popular products - This is a static implementation until we have analytics
    fun getPopularProducts(): Flow<List<Product>> {
        return realtimeDbRepository.getProductsFlow()
    }

    // Search products
    suspend fun searchProducts(query: String): List<Product> {
        val allProducts = mutableListOf<Product>()
        realtimeDbRepository.getProductsFlow().collect { products ->
            allProducts.addAll(products)
        }

        // Filter products based on search query
        return allProducts.filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)
        }
    }

    // Initialize database with sample data if empty
    suspend fun initializeWithSampleDataIfEmpty() {
        try {
            var productsExist = false
            realtimeDbRepository.getProductsFlow().collect { products ->
                productsExist = products.isNotEmpty()
            }

            if (!productsExist) {
                // Populate with sample data
                val sampleProducts = SampleData.getPopularProducts() + SampleData.getRecentlyViewedProducts()
                for (product in sampleProducts) {
                    realtimeDbRepository.addProduct(product)
                }
                Log.d("ProductRepository", "Initialized database with sample data")
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error initializing database", e)
        }
    }
}