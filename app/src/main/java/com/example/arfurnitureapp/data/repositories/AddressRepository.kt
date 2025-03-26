package com.example.arfurnitureapp.data.repositories

import android.util.Log
import com.example.arfurnitureapp.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val TAG = "AddressRepository"

class AddressRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Get all addresses for the current user
     */
    fun getUserAddresses(): Flow<List<Address>> = flow {
        val userId = auth.currentUser?.uid ?: run {
            emit(emptyList<Address>())
            return@flow
        }

        try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("addresses")
                .get()
                .await()

            val addresses = snapshot.documents.mapNotNull { doc ->
                try {
                    // Convert to Address object and ensure ID is set
                    doc.toObject(Address::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting address doc: ${doc.id}", e)
                    null
                }
            }
            emit(addresses)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting addresses", e)
            emit(emptyList())
        }
    }

    /**
     * Get a specific address by ID
     */
    suspend fun getAddressById(addressId: String): Address? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            val doc = db.collection("users")
                .document(userId)
                .collection("addresses")
                .document(addressId)
                .get()
                .await()

            doc.toObject(Address::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address", e)
            null
        }
    }

    /**
     * Add a new address for the current user
     */
    suspend fun addAddress(address: Address): String? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            // If this is set as default, update all other addresses to not be default
            if (address.isDefault) {
                updateAllAddressesToNonDefault()
            }

            // Ensure postcode is uppercase for consistency (UK format)
            val formattedAddress = address.copy(
                id = "", // Remove any ID before adding
                postcode = address.postcode.uppercase()
            )

            val docRef = db.collection("users")
                .document(userId)
                .collection("addresses")
                .add(formattedAddress)
                .await()

            Log.d(TAG, "Address added with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding address", e)
            null
        }
    }

    /**
     * Update an existing address
     */
    suspend fun updateAddress(address: Address): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val addressId = address.id

        if (addressId.isBlank()) {
            Log.e(TAG, "Cannot update address with blank ID")
            return false
        }

        return try {
            // If this is set as default, update all other addresses to not be default
            if (address.isDefault) {
                updateAllAddressesToNonDefault()
            }

            // Ensure postcode is uppercase for consistency (UK format)
            val formattedAddress = address.copy(
                postcode = address.postcode.uppercase()
            )

            db.collection("users")
                .document(userId)
                .collection("addresses")
                .document(addressId)
                .set(formattedAddress)
                .await()

            Log.d(TAG, "Address updated: $addressId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating address", e)
            false
        }
    }

    /**
     * Delete an address
     */
    suspend fun deleteAddress(addressId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            // Check if this is a default address before deleting
            val addressDoc = db.collection("users")
                .document(userId)
                .collection("addresses")
                .document(addressId)
                .get()
                .await()

            val isDefault = addressDoc.getBoolean("isDefault") == true

            // Delete the address
            db.collection("users")
                .document(userId)
                .collection("addresses")
                .document(addressId)
                .delete()
                .await()

            // If we deleted the default address, set a new default if possible
            if (isDefault) {
                setNewDefaultAddressAfterDeletion()
            }

            Log.d(TAG, "Address deleted: $addressId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting address", e)
            false
        }
    }

    /**
     * Set an address as the default shipping address
     */
    suspend fun setDefaultAddress(addressId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            // First, make all addresses non-default
            updateAllAddressesToNonDefault()

            // Then set the specified address as default
            db.collection("users")
                .document(userId)
                .collection("addresses")
                .document(addressId)
                .update("isDefault", true)
                .await()

            Log.d(TAG, "Default address set: $addressId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting default address", e)
            false
        }
    }

    /**
     * Helper method to update all addresses to non-default
     */
    private suspend fun updateAllAddressesToNonDefault(): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("addresses")
                .get()
                .await()

            for (doc in snapshot.documents) {
                db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(doc.id)
                    .update("isDefault", false)
                    .await()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating addresses to non-default", e)
            false
        }
    }

    /**
     * Helper method to set a new default address after the default was deleted
     */
    private suspend fun setNewDefaultAddressAfterDeletion(): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("addresses")
                .limit(1)  // Just get the first available address
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val firstAddressId = snapshot.documents[0].id
                setDefaultAddress(firstAddressId)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting new default address", e)
            false
        }
    }
}