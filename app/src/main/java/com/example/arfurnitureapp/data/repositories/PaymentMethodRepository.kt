package com.example.arfurnitureapp.data.repositories

import android.util.Log
import com.example.arfurnitureapp.model.PaymentMethod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val TAG = "PaymentMethodRepo"

class PaymentMethodRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Get all payment methods for the current user
     */
    fun getUserPaymentMethods(): Flow<List<PaymentMethod>> = flow {
        val userId = auth.currentUser?.uid ?: run {
            emit(emptyList<PaymentMethod>())
            return@flow
        }

        try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("paymentMethods")
                .get()
                .await()

            val paymentMethods = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(PaymentMethod::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting payment method doc: ${doc.id}", e)
                    null
                }
            }
            emit(paymentMethods)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting payment methods", e)
            emit(emptyList())
        }
    }

    /**
     * Get a specific payment method by ID
     */
    suspend fun getPaymentMethodById(paymentMethodId: String): PaymentMethod? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            val doc = db.collection("users")
                .document(userId)
                .collection("paymentMethods")
                .document(paymentMethodId)
                .get()
                .await()

            doc.toObject(PaymentMethod::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting payment method", e)
            null
        }
    }

    /**
     * Add a new payment method for the current user
     */
    suspend fun addPaymentMethod(paymentMethod: PaymentMethod): String? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            // If this is set as default, update all other payment methods to not be default
            if (paymentMethod.isDefault) {
                updateAllPaymentMethodsToNonDefault()
            }

            val paymentMethodWithoutId = paymentMethod.copy(id = "") // Remove any ID before adding

            // Mask the CVV before storing (in a real app, you would use proper encryption)
            val securedPaymentMethod = paymentMethodWithoutId.copy(
                cvv = "***"
            )

            val docRef = db.collection("users")
                .document(userId)
                .collection("paymentMethods")
                .add(securedPaymentMethod)
                .await()

            Log.d(TAG, "Payment method added with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding payment method", e)
            null
        }
    }

    /**
     * Update an existing payment method
     */
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val paymentMethodId = paymentMethod.id

        if (paymentMethodId.isBlank()) {
            Log.e(TAG, "Cannot update payment method with blank ID")
            return false
        }

        return try {
            // If this is set as default, update all other payment methods to not be default
            if (paymentMethod.isDefault) {
                updateAllPaymentMethodsToNonDefault()
            }

            // Mask the CVV before storing
            val securedPaymentMethod = paymentMethod.copy(
                cvv = "***"
            )

            db.collection("users")
                .document(userId)
                .collection("paymentMethods")
                .document(paymentMethodId)
                .set(securedPaymentMethod)
                .await()

            Log.d(TAG, "Payment method updated: $paymentMethodId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment method", e)
            false
        }
    }

    /**
     * Delete a payment method
     */
    suspend fun deletePaymentMethod(paymentMethodId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            db.collection("users")
                .document(userId)
                .collection("paymentMethods")
                .document(paymentMethodId)
                .delete()
                .await()

            Log.d(TAG, "Payment method deleted: $paymentMethodId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting payment method", e)
            false
        }
    }

    /**
     * Set a payment method as the default
     */
    suspend fun setDefaultPaymentMethod(paymentMethodId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            // First, make all payment methods non-default
            updateAllPaymentMethodsToNonDefault()

            // Then set the specified payment method as default
            db.collection("users")
                .document(userId)
                .collection("paymentMethods")
                .document(paymentMethodId)
                .update("isDefault", true)
                .await()

            Log.d(TAG, "Default payment method set: $paymentMethodId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting default payment method", e)
            false
        }
    }

    /**
     * Helper method to update all payment methods to non-default
     */
    private suspend fun updateAllPaymentMethodsToNonDefault(): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("paymentMethods")
                .get()
                .await()

            for (doc in snapshot.documents) {
                db.collection("users")
                    .document(userId)
                    .collection("paymentMethods")
                    .document(doc.id)
                    .update("isDefault", false)
                    .await()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment methods to non-default", e)
            false
        }
    }
}