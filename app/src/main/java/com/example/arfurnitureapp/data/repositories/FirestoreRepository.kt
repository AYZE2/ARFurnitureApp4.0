package com.example.arfurnitureapp.data.repositories

import android.util.Log
import com.example.arfurnitureapp.data.SampleData
import com.example.arfurnitureapp.model.Category
import com.example.arfurnitureapp.model.Product
import com.example.arfurnitureapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepository"

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")
    private val usersCollection = db.collection("users")
    private val categoriesCollection = db.collection("categories")
    private val auth = FirebaseAuth.getInstance()

    // Product operations
    fun getProducts(): Flow<List<Product>> = flow {
        try {
            val snapshot = productsCollection.get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Product::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting product doc: ${doc.id}", e)
                    null
                }
            }
            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting products", e)
            emit(emptyList())
        }
    }

    fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow {
        try {
            val snapshot = productsCollection
                .whereEqualTo("categoryId", categoryId)
                .get().await()

            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)
            }
            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting products by category", e)
            emit(emptyList())
        }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = productsCollection.document(productId).get().await()
            doc.toObject(Product::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by ID", e)
            null
        }
    }

    fun getPopularProducts(): Flow<List<Product>> = flow {
        try {
            // This is just a sample implementation
            // In a real app, you might have a "popularity" field to sort by
            val snapshot = productsCollection
                .limit(10)
                .get().await()

            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)
            }
            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting popular products", e)
            emit(emptyList())
        }
    }

    fun searchProducts(query: String): Flow<List<Product>> = flow {
        try {
            // Firestore doesn't support full-text search natively
            // This is a simple implementation that checks if the query is in the name or description
            // For production, consider using Algolia or another search service
            val snapshot = productsCollection.get().await()

            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)
            }.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
            }

            emit(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching products", e)
            emit(emptyList())
        }
    }

    suspend fun addProduct(product: Product): Boolean {
        return try {
            productsCollection.document(product.id).set(product).await()
            Log.d(TAG, "Product added successfully: ${product.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product", e)
            false
        }
    }

    suspend fun updateProduct(product: Product): Boolean {
        return try {
            productsCollection.document(product.id).set(product).await()
            Log.d(TAG, "Product updated successfully: ${product.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product", e)
            false
        }
    }

    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            productsCollection.document(productId).delete().await()
            Log.d(TAG, "Product deleted successfully: $productId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product", e)
            false
        }
    }

    // Category operations
    fun getCategories(): Flow<List<Category>> = flow {
        try {
            val snapshot = categoriesCollection.get().await()
            val categories = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Category::class.java)
            }
            emit(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting categories", e)
            emit(emptyList())
        }
    }

    suspend fun getCategoryById(categoryId: String): Category? {
        return try {
            val doc = categoriesCollection.document(categoryId).get().await()
            doc.toObject(Category::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category by ID", e)
            null
        }
    }

    suspend fun addCategory(category: Category): Boolean {
        return try {
            categoriesCollection.document(category.id).set(category).await()
            Log.d(TAG, "Category added successfully: ${category.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding category", e)
            false
        }
    }

    // User operations
    suspend fun getUser(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user", e)
            null
        }
    }

    suspend fun saveUser(user: User): Boolean {
        return try {
            usersCollection.document(user.id).set(user).await()
            Log.d(TAG, "User saved successfully: ${user.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
            false
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            usersCollection.document(userId).update(updates).await()
            Log.d(TAG, "User profile updated successfully: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            false
        }
    }

    // Favorites operations
    suspend fun addToFavorites(userId: String, productId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("favorites", com.google.firebase.firestore.FieldValue.arrayUnion(productId))
                .await()
            Log.d(TAG, "Added to favorites: $productId for user $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to favorites", e)
            false
        }
    }

    suspend fun removeFromFavorites(userId: String, productId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("favorites", com.google.firebase.firestore.FieldValue.arrayRemove(productId))
                .await()
            Log.d(TAG, "Removed from favorites: $productId for user $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from favorites", e)
            false
        }
    }

    fun getUserFavorites(userId: String): Flow<List<String>> = flow {
        try {
            val doc = usersCollection.document(userId).get().await()
            @Suppress("UNCHECKED_CAST")
            val favorites = doc.get("favorites") as? List<String> ?: emptyList()
            emit(favorites)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user favorites", e)
            emit(emptyList())
        }
    }

    // Cart operations
    suspend fun addToCart(userId: String, productId: String, quantity: Int): Boolean {
        return try {
            val cartItem = mapOf(productId to quantity)
            usersCollection.document(userId)
                .update("cart.$productId", quantity)
                .await()
            Log.d(TAG, "Added to cart: $productId, quantity: $quantity for user $userId")
            true
        } catch (e: Exception) {
            try {
                // Document might not have cart field yet, try setting instead of updating
                val cartItem = mapOf("cart" to mapOf(productId to quantity))
                usersCollection.document(userId)
                    .set(cartItem, SetOptions.merge())
                    .await()
                Log.d(TAG, "Added to cart (set): $productId, quantity: $quantity for user $userId")
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Error adding to cart", e2)
                false
            }
        }
    }

    suspend fun removeFromCart(userId: String, productId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("cart.$productId", com.google.firebase.firestore.FieldValue.delete())
                .await()
            Log.d(TAG, "Removed from cart: $productId for user $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from cart", e)
            false
        }
    }

    suspend fun clearCart(userId: String): Boolean {
        return try {
            usersCollection.document(userId)
                .update("cart", mapOf<String, Int>())
                .await()
            Log.d(TAG, "Cleared cart for user $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart", e)
            false
        }
    }

    fun getUserCart(userId: String): Flow<Map<String, Int>> = flow {
        try {
            val doc = usersCollection.document(userId).get().await()
            @Suppress("UNCHECKED_CAST")
            val cart = doc.get("cart") as? Map<String, Int> ?: emptyMap()
            emit(cart)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user cart", e)
            emit(emptyMap())
        }
    }

    // Data initialization
    suspend fun populateInitialDataIfEmpty() {
        try {
            val productsSnapshot = productsCollection.limit(1).get().await()
            if (productsSnapshot.isEmpty) {
                Log.d(TAG, "No products found, populating with sample data")

                // Add sample categories
                val categories = SampleData.getCategories()
                for (category in categories) {
                    addCategory(category)
                }

                // Add sample products
                val products = SampleData.getPopularProducts() + SampleData.getRecentlyViewedProducts()
                for (product in products) {
                    addProduct(product)
                }

                Log.d(TAG, "Sample data populated successfully")
            } else {
                Log.d(TAG, "Products already exist, skipping sample data population")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating initial data", e)
        }
    }
    // Add this function to FirestoreRepository
    suspend fun debugUserDocument(userId: String) {
        try {
            val doc = usersCollection.document(userId).get().await()
            Log.d(TAG, "User document exists: ${doc.exists()}")
            if (doc.exists()) {
                Log.d(TAG, "User data: ${doc.data}")

                @Suppress("UNCHECKED_CAST")
                val favorites = doc.get("favorites") as? List<String>
                Log.d(TAG, "Favorites: $favorites")
            } else {
                Log.d(TAG, "User document does not exist")

                // Create basic user document with empty favorites
                val basicUser = User(
                    id = userId,
                    email = auth.currentUser?.email ?: "",
                    name = auth.currentUser?.displayName ?: "User",
                    favorites = emptyList()
                )
                usersCollection.document(userId).set(basicUser).await()
                Log.d(TAG, "Created basic user document with empty favorites")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error debugging user document", e)
        }
    }
}
