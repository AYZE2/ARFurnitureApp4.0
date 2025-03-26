package com.example.arfurnitureapp.data.repositories

import com.example.arfurnitureapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun createUser(name: String, email: String, password: String): User? {
        return try {
            // Create user in Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return null

            // Create user document in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = name
            )

            // Save user to Firestore
            firestore.collection("users").document(firebaseUser.uid)
                .set(user)
                .await()

            user
        } catch (e: Exception) {
            // Log the error or handle specific exceptions
            null
        }
    }

    suspend fun signIn(email: String, password: String): User? {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return null

            // Fetch user details from Firestore
            val userSnapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            userSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }
    // In UserRepository
    suspend fun updateUser(user: User): Boolean {
        return try {
            firestore.collection("users").document(user.id)
                .set(user)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
