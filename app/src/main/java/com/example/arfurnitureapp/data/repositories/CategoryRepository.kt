package com.example.arfurnitureapp.data.repositories

import android.util.Log
import com.example.arfurnitureapp.data.SampleData
import com.example.arfurnitureapp.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class CategoryRepository(private val firestore: FirebaseFirestore) {
    fun getCategories(): Flow<List<Category>> = flow {
        try {
            val snapshot = firestore.collection("categories").get().await()
            val categories = snapshot.documents.map { doc ->
                Category(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    imageResId = doc.getLong("imageResId")?.toInt() ?: 0
                )
            }
            emit(categories)
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Error fetching categories", e)
            emit(emptyList())
        }
    }

    suspend fun populateCategoriesIfEmpty() {
        try {
            val snapshot = firestore.collection("categories").get().await()
            if (snapshot.isEmpty) {
                // Use SampleData to populate Firestore
                SampleData.getCategories().forEach { category ->
                    firestore.collection("categories").document(category.id)
                        .set(mapOf(
                            "name" to category.name,
                            "imageResId" to category.imageResId
                        ))
                }
                Log.d("CategoryRepository", "Populated categories in Firestore")
            }
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Error populating categories", e)
        }
    }
}