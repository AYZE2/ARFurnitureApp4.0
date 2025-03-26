package com.example.arfurnitureapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arfurnitureapp.data.repositories.RealtimeDatabaseRepository
import com.example.arfurnitureapp.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.arfurnitureapp.data.repositories.FirestoreRepository

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val realtimeDbRepository = RealtimeDatabaseRepository()
    private val firestoreRepository = FirestoreRepository()

    // Authentication state
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // User data
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Error states
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _signupError = MutableStateFlow<String?>(null)
    val signupError: StateFlow<String?> = _signupError.asStateFlow()

    private val _updateProfileSuccess = MutableStateFlow<Boolean?>(null)
    val updateProfileSuccess: StateFlow<Boolean?> = _updateProfileSuccess.asStateFlow()

    init {
        // Check if user is already logged in
        auth.currentUser?.let {
            Log.d(TAG, "User already logged in: ${it.uid}")
            fetchUserData(it.uid)
        }
    }

    fun login(email: String, password: String) {
        // Input validation
        if (email.isBlank() || password.isBlank()) {
            _loginError.value = "Email and password are required"
            return
        }

        _isLoading.value = true
        _loginError.value = null
        Log.d(TAG, "Attempting login with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Login successful for user: ${result.user?.uid}")
                val userId = result.user?.uid ?: ""
                if (userId.isEmpty()) {
                    _loginError.value = "Failed to authenticate user"
                    _isLoading.value = false
                    return@addOnSuccessListener
                }

                _isLoggedIn.value = true  // Set logged in state immediately

                // Fetch user data from Realtime Database
                fetchUserData(userId)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Login failed", exception)
                _loginError.value = "Login failed: ${exception.localizedMessage ?: "Unknown error"}"
                _isLoading.value = false
            }
    }

    fun signup(name: String, email: String, password: String) {
        // Input validation
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _signupError.value = "All fields are required"
            return
        }

        if (password.length < 6) {
            _signupError.value = "Password must be at least 6 characters long"
            return
        }

        _isLoading.value = true
        _signupError.value = null
        Log.d(TAG, "Attempting signup with email: $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                if (userId.isEmpty()) {
                    _signupError.value = "Failed to create user account"
                    _isLoading.value = false
                    return@addOnSuccessListener
                }

                Log.d(TAG, "User created successfully in Auth: $userId")

                val newUser = User(id = userId, email = email, name = name,favorites = emptyList() )

                // Save user data in Realtime Database
                viewModelScope.launch {
                    val success = firestoreRepository.saveUser(newUser)

                    if (success) {
                        Log.d(TAG, "User data saved to Realtime Database")
                        _isLoggedIn.value = true
                        _currentUser.value = newUser
                    } else {
                        Log.e(TAG, "Failed to save user data to Realtime Database")
                        _signupError.value = "Account created but profile setup failed. Please try updating your profile."
                        _isLoggedIn.value = true
                        _currentUser.value = newUser  // Use basic user info from auth
                    }
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { authException ->
                Log.e(TAG, "Signup failed", authException)
                _signupError.value = "Signup failed: ${authException.localizedMessage ?: "Unknown error"}"
                _isLoading.value = false
            }
    }

    fun logout() {
        Log.d(TAG, "Logging out user")
        auth.signOut()
        _isLoggedIn.value = false
        _currentUser.value = null
    }
    private fun fetchUserData(userId: String) {
        _isLoading.value = true
        Log.d(TAG, "Fetching user data for ID: $userId")

        viewModelScope.launch {
            try {
                val user = firestoreRepository.getUser(userId)

                if (user != null) {
                    Log.d(TAG, "User data retrieved: ${user.name}")
                    _currentUser.value = user
                } else {
                    Log.d(TAG, "No user data found for ID: $userId")
                    createFallbackUserDocument(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user data", e)
                _loginError.value = "Failed to fetch user data: ${e.localizedMessage ?: "Unknown error"}"
                createFallbackUserDocument(userId)
            } finally {
                _isLoading.value = false
            }
        }
    }



    private fun createFallbackUserDocument(userId: String) {
        // Create a basic user object from auth data
        auth.currentUser?.let { firebaseUser ->
            val basicUser = User(
                id = userId,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "User"
            )

            Log.d(TAG, "Creating fallback user document with name: ${basicUser.name}, email: ${basicUser.email}")

            _currentUser.value = basicUser

            // Save this basic user to Realtime Database
            viewModelScope.launch {
                val success = firestoreRepository.saveUser(basicUser)

                if (success) {
                    Log.d(TAG, "Created new user document in Realtime Database")
                } else {
                    Log.e(TAG, "Failed to create user document in Realtime Database")
                }
                _isLoading.value = false
            }
        } ?: run {
            // If we somehow don't have a current user, set loading to false
            _isLoading.value = false
            _loginError.value = "Error retrieving user information"
        }
    }

    fun updateUserProfile(name: String, phone: String, address: String) {
        val currentUser = _currentUser.value ?: return
        _isLoading.value = true

        val updatedUser = currentUser.copy(name = name, phone = phone, address = address)
        Log.d(TAG, "Updating profile for user: ${currentUser.id}")

        viewModelScope.launch {
            val success = realtimeDbRepository.saveUser(updatedUser)

            if (success) {
                Log.d(TAG, "Profile updated successfully")
                _currentUser.value = updatedUser
                _updateProfileSuccess.value = true
            } else {
                Log.e(TAG, "Failed to update profile")
                _updateProfileSuccess.value = false
                _signupError.value = "Failed to update profile"
            }
            _isLoading.value = false
        }
    }

    // Force create a user document in case something went wrong
    fun forceCreateUserDocument() {
        val currentAuthUser = auth.currentUser ?: run {
            Log.e(TAG, "No authenticated user found when trying to force create document")
            return
        }

        _isLoading.value = true

        val userId = currentAuthUser.uid
        val email = currentAuthUser.email ?: ""
        val displayName = currentAuthUser.displayName ?: "User"

        Log.d(TAG, "Force creating user document for $userId with email $email and name $displayName")

        val user = User(id = userId, email = email, name = displayName)

        viewModelScope.launch {
            val success = realtimeDbRepository.saveUser(user)

            if (success) {
                Log.d(TAG, "Successfully force-created user document")
                _currentUser.value = user
                _isLoggedIn.value = true
            } else {
                Log.e(TAG, "Failed to force-create user document")
            }
            _isLoading.value = false
        }
    }

    // Function to check and fix user document if needed
    fun checkAndFixUserDocument() {
        val currentAuthUser = auth.currentUser
        if (currentAuthUser == null) {
            Log.d(TAG, "No user is authenticated, nothing to fix")
            return
        }

        val userId = currentAuthUser.uid
        Log.d(TAG, "Checking user document for $userId")

        viewModelScope.launch {
            val user = realtimeDbRepository.getUserById(userId)

            if (user == null) {
                Log.d(TAG, "User document missing, creating it now")
                forceCreateUserDocument()
            } else {
                Log.d(TAG, "User document exists")
                _currentUser.value = user
                _isLoggedIn.value = true
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearSignupError() {
        _signupError.value = null
    }

    fun clearUpdateProfileSuccess() {
        _updateProfileSuccess.value = null
    }
}