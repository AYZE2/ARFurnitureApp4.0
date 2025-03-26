package com.example.arfurnitureapp.data.repositories

import android.util.Log
import com.example.arfurnitureapp.model.Order
import com.example.arfurnitureapp.model.OrderItem
import com.example.arfurnitureapp.model.OrderStatus
import com.example.arfurnitureapp.model.Product
import com.example.arfurnitureapp.viewmodel.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

private const val TAG = "OrderRepository"

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val ordersCollection = db.collection("orders")
    private val firestoreRepository = FirestoreRepository()

    /**
     * Get all orders for the current user
     */
    fun getUserOrders(): Flow<List<Order>> = flow {
        val userId = auth.currentUser?.uid ?: run {
            emit(emptyList<Order>())
            return@flow
        }

        try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting order doc: ${doc.id}", e)
                    null
                }
            }
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orders", e)
            emit(emptyList())
        }
    }

    /**
     * Get a specific order by ID
     */
    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val doc = ordersCollection.document(orderId).get().await()
            doc.toObject(Order::class.java)?.copy(orderId = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order", e)
            null
        }
    }

    /**
     * Create a new order from cart items
     */
    suspend fun createOrder(
        cartItems: List<CartItem>,
        shippingAddress: com.example.arfurnitureapp.model.Address,
        billingAddress: com.example.arfurnitureapp.model.Address,
        paymentMethodId: String,
        paymentMethodLast4: String,
        subtotal: Double,
        tax: Double,
        shipping: Double,
        discount: Double
    ): String? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            // Convert cart items to order items
            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    price = cartItem.product.price,
                    imageUrl = cartItem.product.imageResId.toString() // In a real app, this would be a URL
                )
            }

            val total = subtotal + tax + shipping - discount

            val order = Order(
                userId = userId,
                items = orderItems,
                subtotal = subtotal,
                tax = tax,
                shipping = shipping,
                discount = discount,
                total = total,
                shippingAddress = shippingAddress,
                billingAddress = billingAddress,
                paymentMethodId = paymentMethodId,
                paymentMethodLast4 = paymentMethodLast4,
                orderDate = Date(),
                status = OrderStatus.PROCESSING,
                estimatedDeliveryDate = Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)), // 7 days from now
                trackingNumber = generateTrackingNumber()
            )

            val docRef = ordersCollection.add(order).await()

            Log.d(TAG, "Order created with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order", e)
            null
        }
    }

    /**
     * Update order status
     */
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Boolean {
        return try {
            ordersCollection.document(orderId)
                .update("status", status)
                .await()

            Log.d(TAG, "Order status updated: $orderId to $status")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status", e)
            false
        }
    }

    /**
     * Cancel an order
     */
    suspend fun cancelOrder(orderId: String): Boolean {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    /**
     * Get recent orders (for admin)
     */
    fun getRecentOrders(limit: Int = 20): Flow<List<Order>> = flow {
        try {
            val snapshot = ordersCollection
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting order doc: ${doc.id}", e)
                    null
                }
            }
            emit(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent orders", e)
            emit(emptyList())
        }
    }

    // Helper function to generate a tracking number
    private fun generateTrackingNumber(): String {
        val random = UUID.randomUUID().toString().replace("-", "").substring(0, 12).uppercase()
        return "TRK$random"
    }
}