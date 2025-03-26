package com.example.arfurnitureapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.components.FilterDialog
import com.example.arfurnitureapp.components.ProductGridItem
import com.example.arfurnitureapp.components.SearchBar
import com.example.arfurnitureapp.data.SampleData
import com.example.arfurnitureapp.viewmodel.FilterState
import com.example.arfurnitureapp.viewmodel.FilterViewModel
import com.example.arfurnitureapp.viewmodel.SearchViewModel
import com.example.arfurnitureapp.viewmodel.SortOrder
import kotlinx.coroutines.flow.collectLatest

// Updated ProductListScreen.kt with filter functionality
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    categoryId: String,
    navController: NavController,
    searchViewModel: SearchViewModel = viewModel(),
    filterViewModel: FilterViewModel = viewModel()
) {
    val category = SampleData.getCategoryById(categoryId)

    // Get search state
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()

    // Get filter state
    val filterState by filterViewModel.filterState.collectAsState()

    // Apply search first, then filter
    val filteredProducts by remember(filterState, searchResults) {
        derivedStateOf {
            val searchedProducts = searchResults.filter { it.categoryId == categoryId }
            filterViewModel.applyFilters(searchedProducts)
        }
    }

    // State for showing filter dialog
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category?.name ?: "Products") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
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
                placeholder = "Search ${category?.name ?: "products"}..."
            )

            // Show active filters if any
            if (filterState != FilterState()) {
                FilterChip(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    selected = true,
                    onClick = { showFilterDialog = true },
                    label = {
                        Text(
                            when (filterState.sortOrder) {
                                SortOrder.PRICE_LOW_TO_HIGH -> "Price: Low to High"
                                SortOrder.PRICE_HIGH_TO_LOW -> "Price: High to Low"
                                SortOrder.NONE -> "Price: $${filterState.minPrice} - $${filterState.maxPrice}"
                            }
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear Filter",
                            modifier = Modifier.clickable { filterViewModel.resetFilters() }
                        )
                    }
                )
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (searchQuery.isNotEmpty() || filterState != FilterState()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try different search terms or filters",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    searchViewModel.updateSearchQuery("")
                                    filterViewModel.resetFilters()
                                    searchViewModel.search("") // 👈 Ensure all products reload
                                }
                            ) {
                                Text("Clear All Filters")
                            }
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
                    items(filteredProducts) { product ->
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

        // Show filter dialog when needed
        if (showFilterDialog) {
            FilterDialog(
                filterState = filterState,
                onFilterChange = { newFilterState ->
                    filterViewModel.updatePriceRange(newFilterState.minPrice, newFilterState.maxPrice)
                    filterViewModel.updateSortOrder(newFilterState.sortOrder)
                    filterViewModel.toggleInStockFilter(newFilterState.showOnlyInStock) // ✅ Fix stock filter
                },
                onDismiss = { showFilterDialog = false },
                onApply = { showFilterDialog = false }
            )
        }
    }
}
