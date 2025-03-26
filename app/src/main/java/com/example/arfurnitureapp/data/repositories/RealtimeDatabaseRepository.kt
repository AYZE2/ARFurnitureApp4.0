package com.example.arfurnitureapp.data.repositories

import android.util.Log
import com.example.arfurnitureapp.model.Product
import com.example.arfurnitureapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "RealtimeDBRepository"

class RealtimeDatabaseRepository {
    // Initialize Firebase Realtime Database with your specific URL
    private val database = FirebaseDatabase.getInstance("https://arfurniture-f8b1b-default-rtdb.europe-west1.firebasedatabase.app/")
    private val productsRef = database.getReference("products")
    private val usersRef = database.getReference("users")
    private val categoriesRef = database.getReference("categories")

    // Products operations

    /**
     * Get all products as a Flow
     */
    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = mutableListOf<Product>()
                for (child in snapshot.children) {
                    child.getValue<Product>()?.let { product ->
                        productsList.add(product)
                    }
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getProductsFlow:onCancelled", error.toException())
            }
        }

        productsRef.addValueEventListener(listener)

        // Remove the listener when the flow is cancelled
        awaitClose { productsRef.removeEventListener(listener) }
    }

    /**
     * Get products by category
     */
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = mutableListOf<Product>()
                for (child in snapshot.children) {
                    child.getValue<Product>()?.let { product ->
                        if (product.categoryId == categoryId) {
                            productsList.add(product)
                        }
                    }
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getProductsByCategory:onCancelled", error.toException())
            }
        }

        productsRef.addValueEventListener(listener)

        // Remove the listener when the flow is cancelled
        awaitClose { productsRef.removeEventListener(listener) }
    }

    /**
     * Get a product by ID
     */
    suspend fun getProductById(productId: String): Product? {
        return try {
            val snapshot = productsRef.child(productId).get().await()
            snapshot.getValue<Product>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by ID", e)
            null
        }
    }

    /**
     * Add a new product
     */
    suspend fun addProduct(product: Product): Boolean {
        return try {
            productsRef.child(product.id).setValue(product).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product", e)
            false
        }
    }

    /**
     * Update an existing product
     */
    suspend fun updateProduct(product: Product): Boolean {
        return try {
            productsRef.child(product.id).setValue(product).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product", e)
            false
        }
    }

    /**
     * Delete a product
     */
    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            productsRef.child(productId).removeValue().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product", e)
            false
        }
    }

    // User operations

    /**
     * Get a user by ID
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            snapshot.getValue<User>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID", e)
            null
        }
    }

    /**
     * Save a user
     */
    suspend fun saveUser(user: User): Boolean {
        return try {
            usersRef.child(user.id).setValue(user).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
            false
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            usersRef.child(userId).updateChildren(updates).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            false
        }
    }

    // Favorites operations

    /**
     * Add product to user favorites
     */
    suspend fun addToFavorites(userId: String, productId: String): Boolean {
        return try {
            usersRef.child(userId).child("favorites").child(productId).setValue(true).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to favorites", e)
            false
        }
    }

    /**
     * Remove product from user favorites
     */
    suspend fun removeFromFavorites(userId: String, productId: String): Boolean {
        return try {
            usersRef.child(userId).child("favorites").child(productId).removeValue().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from favorites", e)
            false
        }
    }

    /**
     * Get user favorites
     */
    fun getUserFavorites(userId: String): Flow<List<String>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { productId ->
                        favorites.add(productId)
                    }
                }
                trySend(favorites)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getUserFavorites:onCancelled", error.toException())
            }
        }

        usersRef.child(userId).child("favorites").addValueEventListener(listener)

        // Remove the listener when the flow is cancelled
        awaitClose { usersRef.child(userId).child("favorites").removeEventListener(listener) }
    }

    // Cart operations

    /**
     * Add product to user cart
     */
    suspend fun addToCart(userId: String, productId: String, quantity: Int): Boolean {
        return try {
            usersRef.child(userId).child("cart").child(productId).setValue(quantity).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            false
        }
    }

    /**
     * Update cart item quantity
     */
    suspend fun updateCartItemQuantity(userId: String, productId: String, quantity: Int): Boolean {
        return try {
            if (quantity > 0) {
                usersRef.child(userId).child("cart").child(productId).setValue(quantity).await()
            } else {
                usersRef.child(userId).child("cart").child(productId).removeValue().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart item quantity", e)
            false
        }
    }

    /**
     * Remove product from user cart
     */
    suspend fun removeFromCart(userId: String, productId: String): Boolean {
        return try {
            usersRef.child(userId).child("cart").child(productId).removeValue().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from cart", e)
            false
        }
    }

    /**
     * Clear user cart
     */
    suspend fun clearCart(userId: String): Boolean {
        return try {
            usersRef.child(userId).child("cart").removeValue().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart", e)
            false
        }
    }

    /**
     * Get user cart
     */
    fun getUserCart(userId: String): Flow<Map<String, Int>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cart = mutableMapOf<String, Int>()
                for (child in snapshot.children) {
                    child.key?.let { productId ->
                        child.getValue<Int>()?.let { quantity ->
                            cart[productId] = quantity
                        }
                    }
                }
                trySend(cart)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getUserCart:onCancelled", error.toException())
            }
        }

        usersRef.child(userId).child("cart").addValueEventListener(listener)

        // Remove the listener when the flow is cancelled
        awaitClose { usersRef.child(userId).child("cart").removeEventListener(listener) }
    }

    // Helper function to populate initial data if needed
    suspend fun populateInitialData(products: List<Product>) {
        try {
            // First check if we already have data
            val snapshot = productsRef.get().await()
            if (!snapshot.exists()) {
                // No data exists, populate with initial data
                for (product in products) {
                    productsRef.child(product.id).setValue(product).await()
                }
                Log.d(TAG, "Initial product data populated successfully")
            } else {
                Log.d(TAG, "Products already exist, skipping initial population")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating initial data", e)
        }
    }
}