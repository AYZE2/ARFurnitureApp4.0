package com.example.arfurnitureapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.components.ProductGridItem
import com.example.arfurnitureapp.components.SearchBar
import com.example.arfurnitureapp.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    searchViewModel: SearchViewModel = viewModel()
) {
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            SearchBar(
                initialQuery = searchQuery,
                onSearch = { query ->
                    searchViewModel.search(query)
                },
                onQueryChange = { query ->
                    searchViewModel.updateSearchQuery(query)
                },
                placeholder = "Search all products..."
            )

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "Search for products" else "No products found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try a different search term",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { product ->
                        ProductGridItem(
                            product = product,
                            onClick = {
                                navController.navigate("productDetail/${product.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}