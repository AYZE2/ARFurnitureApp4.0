package com.example.arfurnitureapp.data

import com.example.arfurnitureapp.R
import com.example.arfurnitureapp.model.Category
import com.example.arfurnitureapp.model.Product

object SampleData {
    fun getCategories(): List<Category> {
        return listOf(
            Category("sofas", "Sofas", R.drawable.category_sofa),
            Category("tables", "Tables", R.drawable.category_table),
            Category("chairs", "Chairs", R.drawable.category_sofa),
            Category("beds", "Beds", R.drawable.category_bed),
            Category("storage", "Storage", R.drawable.category_storage),
            Category("lighting", "Lighting", R.drawable.category_lighting)
        )
    }

    fun getCategoryById(id: String): Category? {
        return getCategories().find { it.id == id }
    }

    fun getPopularProducts(): List<Product> {
        return listOf(
            Product(
                "p1",
                "KLIPPAN Sofa",
                "Compact sofa with a stylish design that's been a favorite since the 1980s. The cover is easy to keep clean as it is removable and can be machine washed.",
                349.99,
                "Sofas",
                "sofas",
                R.drawable.product_sofa,
                R.raw.modern_sofa

            ),
            Product(
                "p2",
                "LISABO Table",
                "Round table with a clear-lacquered ash veneer surface. The table's unique wedge dowels make assembly quick and easy and the construction strong.",
                149.99,
                "Tables",
                "tables",
                R.drawable.product_table,
                R.raw.model_table

            ),
            Product(
                "p3",
                "POÄNG Chair",
                "Bentwood armchair with a cushioned seat in various colors. The layer-glued bent oak frame gives comfortable resilience. A perfect addition to any living room.",
                129.99,
                "Chairs",
                "chairs",
                R.drawable.product_chair,
                R.raw.model_chair

            ),
            Product(
                "p4",
                "MALM Bed Frame",
                "Clean design bed frame with or without storage boxes. Made of real wood veneer, it gives a warm and natural feeling to your bedroom.",
                249.99,
                "Beds",
                "beds",
                R.drawable.product_bed,
                R.raw.model_bed

            )
        )
    }

    fun getRecentlyViewedProducts(): List<Product> {
        return listOf(
            Product(
                "p5",
                "BILLY Bookcase",
                "Iconic bookcase that's a practical storage solution for all your books. Adjustable shelves allow you to customize the space according to your needs.",
                79.99,
                "Storage",
                "storage",
                R.drawable.product_bookcase,
                R.raw.model_bookcase

            ),
            Product(
                "p6",
                "HEKTAR Pendant lamp",
                "Adjustable pendant lamp providing a directed light. Perfect for hanging over a dining table or in a hallway.",
                49.99,
                "Lighting",
                "lighting",
                R.drawable.product_lamp,
                R.raw.model_lamp

            ),
            Product(
                "p7",
                "KALLAX Shelf unit",
                "Versatile shelf unit that can be used horizontally or vertically. Can be enhanced with inserts and boxes to create a personalized storage solution.",
                89.99,
                "Storage",
                "storage",
                R.drawable.product_shelf,
                R.raw.model_chair

            )
        )
    }

    fun getSavedProducts(): List<Product> {
        // In a real app, this would come from a database or shared preferences
        return listOf(
            getProductById("p1")!!,
            getProductById("p5")!!
        )
    }

    fun getProductsByCategory(categoryId: String): List<Product> {
        return (getPopularProducts() + getRecentlyViewedProducts()).filter {
            it.categoryId == categoryId
        }
    }

    fun getProductById(id: String): Product? {
        return (getPopularProducts() + getRecentlyViewedProducts()).find { it.id == id }
    }
}