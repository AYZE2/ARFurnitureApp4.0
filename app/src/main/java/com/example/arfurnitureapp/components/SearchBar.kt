package com.example.arfurnitureapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    initialQuery: String = "",
    onSearch: (String) -> Unit,
    onQueryChange: (String) -> Unit = {},
    placeholder: String = "Search furniture"
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = searchQuery,
        onValueChange = {
            searchQuery = it
            onQueryChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = {
                    searchQuery = ""
                    onQueryChange("")
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(searchQuery)
                focusManager.clearFocus()
            }
        ),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        )
    )
}
