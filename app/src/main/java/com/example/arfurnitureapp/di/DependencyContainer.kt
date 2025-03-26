package com.example.arfurnitureapp.di

import com.example.arfurnitureapp.data.repositories.FirestoreRepository

object DependencyContainer {
    // Create instances of repositories
    val firestoreRepository = FirestoreRepository()

    // Backward compatibility
    val productRepository get() = firestoreRepository
    val categoryRepository = object {
        suspend fun populateCategoriesIfEmpty() {
            firestoreRepository.populateInitialDataIfEmpty()
        }
    }
}