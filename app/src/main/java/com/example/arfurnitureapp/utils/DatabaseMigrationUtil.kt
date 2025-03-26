package com.example.arfurnitureapp.utils

import android.content.Context
import android.util.Log
import com.example.arfurnitureapp.data.SampleData
import com.example.arfurnitureapp.data.repositories.RealtimeDatabaseRepository
import com.example.arfurnitureapp.model.Category
import com.example.arfurnitureapp.model.Product
import com.example.arfurnitureapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Utility class to help migrate data from Firestore to Realtime Database
 * or populate Realtime Database with initial sample data.
 */
object DatabaseMigrationUtil {
    private const val TAG = "DatabaseMigrationUtil"

    // Instance of Firestore and Realtime Database Repository
    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeRepository = RealtimeDatabaseRepository()

    /**
     * Migrate all users from Firestore to Realtime Database
     */
    suspend fun migrateUsers(): Int = withContext(Dispatchers.IO) {
        var migratedCount = 0
        try {
            val usersSnapshot = firestore.collection("users").get().await()

            for (document in usersSnapshot.documents) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    val success = realtimeRepository.saveUser(user)
                    if (success) migratedCount++
                }
            }

            Log.d(TAG, "Migrated $migratedCount users to Realtime Database")
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating users", e)
        }

        return@withContext migratedCount
    }

    /**
     * Migrate all products from Firestore to Realtime Database
     */
    suspend fun migrateProducts(): Int = withContext(Dispatchers.IO) {
        var migratedCount = 0
        try {
            val productsSnapshot = firestore.collection("products").get().await()

            for (document in productsSnapshot.documents) {
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    val success = realtimeRepository.addProduct(product)
                    if (success) migratedCount++
                }
            }

            Log.d(TAG, "Migrated $migratedCount products to Realtime Database")
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating products", e)
        }

        return@withContext migratedCount
    }

    /**
     * Migrate user favorites from Firestore to Realtime Database
     */
    suspend fun migrateUserFavorites(userId: String): Int = withContext(Dispatchers.IO) {
        var migratedCount = 0
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()

            @Suppress("UNCHECKED_CAST")
            val favorites = userDoc.get("favorites") as? List<String> ?: emptyList()

            for (productId in favorites) {
                val success = realtimeRepository.addToFavorites(userId, productId)
                if (success) migratedCount++
            }

            Log.d(TAG, "Migrated $migratedCount favorites for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating favorites for user $userId", e)
        }

        return@withContext migratedCount
    }

    /**
     * Migrate user cart from Firestore to Realtime Database
     */
    suspend fun migrateUserCart(userId: String): Int = withContext(Dispatchers.IO) {
        var migratedCount = 0
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()

            @Suppress("UNCHECKED_CAST")
            val cart = userDoc.get("cart") as? Map<String, Long> ?: emptyMap()

            for ((productId, quantity) in cart) {
                val success = realtimeRepository.addToCart(userId, productId, quantity.toInt())
                if (success) migratedCount++
            }

            Log.d(TAG, "Migrated $migratedCount cart items for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating cart for user $userId", e)
        }

        return@withContext migratedCount
    }

    /**
     * Populate Realtime Database with sample data
     */
    suspend fun populateSampleData(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Populate products
            val products = SampleData.getPopularProducts() + SampleData.getRecentlyViewedProducts()
            realtimeRepository.populateInitialData(products)

            Log.d(TAG, "Successfully populated sample data")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error populating sample data", e)
            return@withContext false
        }
    }

    /**
     * Check if migration is needed by looking at Realtime Database
     */
    suspend fun isMigrationNeeded(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if any products exist in Realtime Database
            var hasProducts = false
            realtimeRepository.getProductsFlow().collect { products ->
                hasProducts = products.isNotEmpty()
            }

            return@withContext !hasProducts
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if migration is needed", e)
            return@withContext true  // Assume migration is needed if error occurs
        }
    }
}