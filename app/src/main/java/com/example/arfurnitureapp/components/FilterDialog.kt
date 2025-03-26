package com.example.arfurnitureapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arfurnitureapp.viewmodel.FilterState
import com.example.arfurnitureapp.viewmodel.SortOrder

@Composable
fun FilterDialog(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    var minPrice by remember { mutableStateOf(filterState.minPrice.toString()) }
    var maxPrice by remember { mutableStateOf(filterState.maxPrice.toString()) }
    var selectedSortOrder by remember { mutableStateOf(filterState.sortOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Products") },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Price Range", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Sort By", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSortOrder == SortOrder.NONE,
                            onClick = { selectedSortOrder = SortOrder.NONE }
                        )
                        Text("None")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSortOrder == SortOrder.PRICE_LOW_TO_HIGH,
                            onClick = { selectedSortOrder = SortOrder.PRICE_LOW_TO_HIGH }
                        )
                        Text("Price: Low to High")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSortOrder == SortOrder.PRICE_HIGH_TO_LOW,
                            onClick = { selectedSortOrder = SortOrder.PRICE_HIGH_TO_LOW }
                        )
                        Text("Price: High to Low")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onFilterChange(
                        FilterState(
                            minPrice = minPrice.toDoubleOrNull() ?: 0.0,
                            maxPrice = maxPrice.toDoubleOrNull() ?: 1000.0,
                            sortOrder = selectedSortOrder
                        )
                    )
                    onApply()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}