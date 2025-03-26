package com.example.arfurnitureapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.R
import com.example.arfurnitureapp.components.BottomNavigationBar
import com.example.arfurnitureapp.model.Address
import com.example.arfurnitureapp.model.PaymentMethod
import com.example.arfurnitureapp.viewmodel.AddressViewModel
import com.example.arfurnitureapp.viewmodel.PaymentMethodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodManagementScreen(
    navController: NavController,
    paymentMethodViewModel: PaymentMethodViewModel = viewModel(),
    addressViewModel: AddressViewModel = viewModel()
) {
    val paymentMethods by paymentMethodViewModel.paymentMethods.collectAsState()
    val addresses by addressViewModel.addresses.collectAsState()
    val isLoading by paymentMethodViewModel.isLoading.collectAsState()
    val error by paymentMethodViewModel.error.collectAsState()
    val successMessage by paymentMethodViewModel.successMessage.collectAsState()

    // State for add/edit payment method dialog
    var showPaymentDialog by remember { mutableStateOf(false) }
    var editingPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    // Show success message with Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error, successMessage) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            paymentMethodViewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            paymentMethodViewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingPaymentMethod = null
                        showPaymentDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Payment Method")
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
        if (isLoading && paymentMethods.isEmpty()) {
            // Show loading spinner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (paymentMethods.isEmpty()) {
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
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No payment methods found",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add a payment method to make checkout faster",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        editingPaymentMethod = null
                        showPaymentDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD PAYMENT METHOD")
                }
            }
        } else {
            // Show list of payment methods
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(paymentMethods) { paymentMethod ->
                    PaymentMethodCard(
                        paymentMethod = paymentMethod,
                        onEdit = {
                            editingPaymentMethod = paymentMethod
                            showPaymentDialog = true
                        },
                        onDelete = {
                            paymentMethodViewModel.deletePaymentMethod(paymentMethod.id)
                        },
                        onSetDefault = {
                            if (!paymentMethod.isDefault) {
                                paymentMethodViewModel.setDefaultPaymentMethod(paymentMethod.id)
                            }
                        }
                    )
                }

                item {
                    // Add new payment method button at the bottom
                    OutlinedButton(
                        onClick = {
                            editingPaymentMethod = null
                            showPaymentDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD PAYMENT METHOD")
                    }

                    // Add some space at the bottom
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Payment method dialog
    if (showPaymentDialog) {
        PaymentMethodDialog(
            paymentMethod = editingPaymentMethod,
            addresses = addresses,
            onDismiss = { showPaymentDialog = false },
            onSave = { newPaymentMethod ->
                if (editingPaymentMethod == null) {
                    // Adding new payment method
                    paymentMethodViewModel.addPaymentMethod(
                        cardHolderName = newPaymentMethod.cardHolderName,
                        cardNumber = newPaymentMethod.cardNumber,
                        expiryDate = newPaymentMethod.expiryDate,
                        cvv = newPaymentMethod.cvv,
                        cardType = newPaymentMethod.cardType,
                        billingAddress = newPaymentMethod.billingAddress,
                        isDefault = newPaymentMethod.isDefault
                    )
                } else {
                    // Updating existing payment method (only certain fields can be updated)
                    paymentMethodViewModel.updatePaymentMethod(
                        paymentMethodId = editingPaymentMethod.id,
                        cardHolderName = newPaymentMethod.cardHolderName,
                        expiryDate = newPaymentMethod.expiryDate,
                        cardType = newPaymentMethod.cardType,
                        billingAddress = newPaymentMethod.billingAddress,
                        isDefault = newPaymentMethod.isDefault
                    )
                }
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun PaymentMethodCard(
    paymentMethod: PaymentMethod,
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
            // Card type and default badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Card type icon
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = paymentMethod.cardType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (paymentMethod.isDefault) {
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

            // Card details
            Column(
                modifier = Modifier.padding(start = 32.dp)
            ) {
                Text(
                    text = paymentMethod.cardNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = paymentMethod.cardHolderName,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Expires: ${paymentMethod.expiryDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                paymentMethod.billingAddress?.let { address ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Billing: ${address.city}, ${address.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Set as default button (only show if not default)
                if (!paymentMethod.isDefault) {
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
fun PaymentMethodDialog(
    paymentMethod: PaymentMethod?,
    addresses: List<Address>,
    onDismiss: () -> Unit,
    onSave: (PaymentMethod) -> Unit
) {
    var cardHolderName by remember { mutableStateOf(paymentMethod?.cardHolderName ?: "") }
    var cardNumber by remember { mutableStateOf(if (paymentMethod == null) "" else "•••• •••• •••• ${paymentMethod.cardNumber.takeLast(4)}") }
    var expiryDate by remember { mutableStateOf(paymentMethod?.expiryDate ?: "") }
    var cvv by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf(paymentMethod?.cardType ?: "Visa") }
    var selectedBillingAddressId by remember { mutableStateOf(paymentMethod?.billingAddress?.id ?: "") }
    var isDefault by remember { mutableStateOf(paymentMethod?.isDefault ?: false) }

    // Card types
    val cardTypes = listOf("Visa", "Mastercard", "Amex", "Discover")

    // Check if editing or adding new
    val isEditing = paymentMethod != null

    // Validate form
    val isFormValid = cardHolderName.isNotBlank() &&
            (isEditing || (cardNumber.isNotBlank() && cvv.isNotBlank())) &&
            expiryDate.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Payment Method" else "Add Payment Method") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Card Type Selection
                Text(
                    text = "Card Type",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cardTypes.forEach { type ->
                        FilterChip(
                            selected = cardType == type,
                            onClick = { cardType = type },
                            label = { Text(type) },
                            leadingIcon = if (cardType == type) {
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
                }

                // Card Holder Name
                OutlinedTextField(
                    value = cardHolderName,
                    onValueChange = { cardHolderName = it },
                    label = { Text("Cardholder Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // Card Number (only editable when adding new)
                if (!isEditing) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null
                            )
                        }
                    )
                } else {
                    // Display masked card number when editing
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { },
                        label = { Text("Card Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        singleLine = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null
                            )
                        }
                    )
                }

                // Expiry Date and CVV
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = {
                            // Format as MM/YY
                            if (it.length <= 5) {
                                var formatted = it.filter { char -> char.isDigit() || char == '/' }
                                if (it.length == 2 && expiryDate.length == 1 && !it.contains('/')) {
                                    formatted = "$it/"
                                }
                                expiryDate = formatted
                            }
                        },
                        label = { Text("Expiry (MM/YY)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        placeholder = { Text("MM/YY") }
                    )

                    if (!isEditing) {
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    cvv = it
                                }
                            },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            )
                        )
                    }
                }

                // Billing Address Selection
                if (addresses.isNotEmpty()) {
                    Text(
                        text = "Billing Address",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    addresses.forEach { address ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedBillingAddressId == address.id,
                                onClick = { selectedBillingAddressId = address.id }
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "${address.label}: ${address.fullName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "${address.city}, ${address.state} ${address.zipCode}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    // No addresses available
                    Text(
                        text = "No billing addresses found. Please add an address first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

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
                        text = "Set as default payment method",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Find selected billing address
                    val billingAddress = addresses.find { it.id == selectedBillingAddressId }

                    val newPaymentMethod = PaymentMethod(
                        id = paymentMethod?.id ?: "",
                        cardHolderName = cardHolderName,
                        cardNumber = if (isEditing) paymentMethod!!.cardNumber else cardNumber,
                        expiryDate = expiryDate,
                        cvv = if (isEditing) "***" else cvv,
                        cardType = cardType,
                        billingAddress = billingAddress,
                        isDefault = isDefault
                    )
                    onSave(newPaymentMethod)
                },
                enabled = isFormValid && (addresses.isNotEmpty() || selectedBillingAddressId.isNotBlank())
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