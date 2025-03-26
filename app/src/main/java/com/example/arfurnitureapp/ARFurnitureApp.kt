package com.example.arfurnitureapp

import android.app.Application
import android.util.Log
import com.example.arfurnitureapp.di.DependencyContainer
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ARFurnitureApp : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize database
        initializeDatabase()
    }

    private fun initializeDatabase() {
        applicationScope.launch {
            try {
                // Populate Firestore with initial data if empty
                DependencyContainer.firestoreRepository.populateInitialDataIfEmpty()
            } catch (e: Exception) {
                Log.e("ARFurnitureApp", "Error initializing database", e)
            }
        }
    }
}