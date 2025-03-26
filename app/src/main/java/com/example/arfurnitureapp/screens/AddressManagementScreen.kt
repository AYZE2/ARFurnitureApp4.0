package com.example.arfurnitureapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.model.Address
import com.example.arfurnitureapp.viewmodel.AddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagementScreen(
    navController: NavController,
    addressViewModel: AddressViewModel = viewModel()
) {
    val addresses by addressViewModel.addresses.collectAsState()
    val isLoading by addressViewModel.isLoading.collectAsState()
    val error by addressViewModel.error.collectAsState()
    val successMessage by addressViewModel.successMessage.collectAsState()

    // State for add/edit address dialog
    var showAddressDialog by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<Address?>(null) }

    // Show success message with Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error, successMessage) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            addressViewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            addressViewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Addresses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingAddress = null
                        showAddressDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Address")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (isLoading && addresses.isEmpty()) {
            // Show loading spinner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (addresses.isEmpty()) {
            // Show empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No addresses found",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add a new address to manage your shipping destinations",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        editingAddress = null
                        showAddressDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD NEW ADDRESS")
                }
            }
        } else {
            // Show list of addresses
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(addresses) { address ->
                    AddressCard(
                        address = address,
                        onEdit = {
                            editingAddress = address
                            showAddressDialog = true
                        },
                        onDelete = {
                            addressViewModel.deleteAddress(address.id)
                        },
                        onSetDefault = {
                            if (!address.isDefault) {
                                addressViewModel.setDefaultAddress(address.id)
                            }
                        }
                    )
                }

                item {
                    // Add new address button at the bottom
                    OutlinedButton(
                        onClick = {
                            editingAddress = null
                            showAddressDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD NEW ADDRESS")
                    }

                    // Add some space at the bottom
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Address dialog
    if (showAddressDialog) {
        AddressDialog(
            address = editingAddress,
            onDismiss = { showAddressDialog = false },
            onSave = { newAddress ->
                if (editingAddress == null) {
                    // Adding new address
                    addressViewModel.addAddress(
                        fullName = newAddress.fullName,
                        phoneNumber = newAddress.phoneNumber,
                        addressLine1 = newAddress.addressLine1,
                        addressLine2 = newAddress.addressLine2,
                        town = newAddress.town,
                        county = newAddress.county,
                        postcode = newAddress.postcode,
                        country = newAddress.country,
                        isDefault = newAddress.isDefault,
                        label = newAddress.label
                    )
                } else {
                    // Updating existing address
                    addressViewModel.updateAddress(
                        addressId = editingAddress!!.id,
                        fullName = newAddress.fullName,
                        phoneNumber = newAddress.phoneNumber,
                        addressLine1 = newAddress.addressLine1,
                        addressLine2 = newAddress.addressLine2,
                        town = newAddress.town,
                        county = newAddress.county,
                        postcode = newAddress.postcode,
                        country = newAddress.country,
                        isDefault = newAddress.isDefault,
                        label = newAddress.label
                    )
                }
                showAddressDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Address label and default badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (address.isDefault) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "Default",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address details - UK Format
            Column(
                modifier = Modifier.padding(start = 32.dp)
            ) {
                Text(
                    text = address.fullName,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = address.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = address.addressLine1,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (address.addressLine2.isNotBlank()) {
                    Text(
                        text = address.addressLine2,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = address.town + (if (address.county.isNotBlank()) ", ${address.county}" else ""),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = address.postcode.uppercase(),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = address.country,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Set as default button (only show if not default)
                if (!address.isDefault) {
                    OutlinedButton(onClick = onSetDefault) {
                        Text("SET AS DEFAULT")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddressDialog(
    address: Address?,
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    var fullName by remember { mutableStateOf(address?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(address?.phoneNumber ?: "") }
    var addressLine1 by remember { mutableStateOf(address?.addressLine1 ?: "") }
    var addressLine2 by remember { mutableStateOf(address?.addressLine2 ?: "") }
    var town by remember { mutableStateOf(address?.town ?: "") }
    var county by remember { mutableStateOf(address?.county ?: "") }
    var postcode by remember { mutableStateOf(address?.postcode ?: "") }
    var country by remember { mutableStateOf(address?.country ?: "United Kingdom") }
    var isDefault by remember { mutableStateOf(address?.isDefault ?: false) }
    var label by remember { mutableStateOf(address?.label ?: "Home") }

    // Validate form
    val isFormValid = fullName.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            addressLine1.isNotBlank() &&
            town.isNotBlank() &&
            postcode.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (address == null) "Add New Address" else "Edit Address") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Address label selection
                Text(
                    text = "Address Type",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip(
                        selected = label == "Home",
                        onClick = { label = "Home" },
                        label = "Home"
                    )

                    SelectableChip(
                        selected = label == "Work",
                        onClick = { label = "Work" },
                        label = "Work"
                    )

                    SelectableChip(
                        selected = label != "Home" && label != "Work",
                        onClick = { label = "Other" },
                        label = "Other"
                    )
                }

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Phone Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Address Line 1
                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it },
                    label = { Text("Address Line 1") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Address Line 2 (optional)
                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address Line 2 (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Town/City
                OutlinedTextField(
                    value = town,
                    onValueChange = { town = it },
                    label = { Text("Town/City") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // County and Postcode (side by side)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = county,
                        onValueChange = { county = it },
                        label = { Text("County (optional)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = postcode,
                        onValueChange = { postcode = it },
                        label = { Text("Postcode") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Country field (usually pre-filled with UK)
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    enabled = false // Disabled for UK-specific app
                )

                // Set as default checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it }
                    )

                    Text(
                        text = "Set as default address",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newAddress = Address(
                        id = address?.id ?: "",
                        fullName = fullName,
                        phoneNumber = phoneNumber,
                        addressLine1 = addressLine1,
                        addressLine2 = addressLine2,
                        town = town,
                        county = county,
                        postcode = postcode.uppercase(), // Ensure postcode is uppercase
                        country = country,
                        isDefault = isDefault,
                        label = label
                    )
                    onSave(newAddress)
                },
                enabled = isFormValid
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}