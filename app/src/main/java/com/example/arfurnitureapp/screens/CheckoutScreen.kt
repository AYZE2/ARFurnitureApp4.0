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
import com.example.arfurnitureapp.model.OrderSummary
import com.example.arfurnitureapp.viewmodel.CartViewModel
import com.example.arfurnitureapp.viewmodel.CheckoutViewModel
import com.example.arfurnitureapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel = viewModel(),
    checkoutViewModel: CheckoutViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentStep by checkoutViewModel.currentStep.collectAsState()
    val orderSummary by checkoutViewModel.orderSummary.collectAsState()

    // Check if user is logged in
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo("checkout") { inclusive = true }
            }
        }
    }

    // Check if cart is empty
    LaunchedEffect(cartItems) {
        if (cartItems.isEmpty()) {
            navController.navigate("cart") {
                popUpTo("checkout") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Checkout Steps
            CheckoutProgressIndicator(currentStep = currentStep)

            Spacer(modifier = Modifier.height(24.dp))

            // Content based on current step
            when (currentStep) {
                1 -> {
                    // Review Cart Items
                    ReviewCartStep(
                        cartItems = cartItems,
                        totalPrice = totalPrice,
                        onContinue = { checkoutViewModel.nextStep() }
                    )
                }
                2 -> {
                    // Address Selection
                    AddressSelectionStep(
                        checkoutViewModel = checkoutViewModel,
                        onBack = { checkoutViewModel.previousStep() },
                        onContinue = { checkoutViewModel.nextStep() }
                    )
                }
                3 -> {
                    // Payment Method
                    PaymentMethodStep(
                        checkoutViewModel = checkoutViewModel,
                        onBack = { checkoutViewModel.previousStep() },
                        onContinue = { checkoutViewModel.nextStep() }
                    )
                }
                4 -> {
                    // Order Summary
                    OrderSummaryStep(
                        cartItems = cartItems,
                        totalPrice = totalPrice,
                        checkoutViewModel = checkoutViewModel,
                        onBack = { checkoutViewModel.previousStep() },
                        onPlaceOrder = {
                            val success = checkoutViewModel.placeOrder(cartItems, totalPrice)
                            if (success) {
                                checkoutViewModel.nextStep()
                            }
                        }
                    )
                }
                5 -> {
                    // Order Confirmation
                    OrderConfirmationStep(
                        orderSummary = orderSummary,
                        onFinish = {
                            cartViewModel.clearCart()
                            checkoutViewModel.resetCheckout()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutProgressIndicator(currentStep: Int) {
    val steps = listOf("Review", "Address", "Payment", "Summary", "Confirmation")

    Column {
        LinearProgressIndicator(
            progress = currentStep / steps.size.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Step $currentStep: ${steps[currentStep - 1]}",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun ReviewCartStep(
    cartItems: List<com.example.arfurnitureapp.viewmodel.CartItem>,
    totalPrice: Double,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Review Your Cart",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cartItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.quantity}x",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.width(40.dp)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = item.product.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = item.product.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$${String.format("%.2f", item.product.price * item.quantity)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order summary
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subtotal",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Estimated Tax",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$${String.format("%.2f", totalPrice * 0.08)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Shipping",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (totalPrice > 50) "FREE" else "$9.99",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    val shipping = if (totalPrice > 50) 0.0 else 9.99
                    Text(
                        text = "$${String.format("%.2f", totalPrice + (totalPrice * 0.08) + shipping)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CONTINUE TO SHIPPING")
        }
    }
}

@Composable
fun AddressSelectionStep(
    checkoutViewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val addresses by checkoutViewModel.addresses.collectAsState()
    val selectedAddress by checkoutViewModel.selectedAddress.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Shipping Address",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (addresses.isEmpty()) {
            // No addresses
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No addresses found",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* Navigate to add address screen */ }
                    ) {
                        Text("ADD NEW ADDRESS")
                    }
                }
            }
        } else {
            // List of addresses
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addresses) { address ->
                    val isSelected = selectedAddress == address

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { checkoutViewModel.selectAddress(address) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = address.fullName,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = address.addressLine1,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "${address.town}, ${address.county} ${address.postcode}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = address.country,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (address.isDefault) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Default",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Add new address button
                    OutlinedButton(
                        onClick = { /* Navigate to add address screen */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD NEW ADDRESS")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("BACK")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f),
                enabled = selectedAddress != null
            ) {
                Text("CONTINUE")
            }
        }
    }
}

@Composable
fun PaymentMethodStep(
    checkoutViewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val paymentMethods by checkoutViewModel.paymentMethods.collectAsState()
    val selectedPaymentMethod by checkoutViewModel.selectedPaymentMethod.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (paymentMethods.isEmpty()) {
            // No payment methods
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No payment methods found",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* Navigate to add payment method screen */ }
                    ) {
                        Text("ADD PAYMENT METHOD")
                    }
                }
            }
        } else {
            // List of payment methods
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(paymentMethods) { paymentMethod ->
                    val isSelected = selectedPaymentMethod == paymentMethod

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { checkoutViewModel.selectPaymentMethod(paymentMethod) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = paymentMethod.cardNumber,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = paymentMethod.cardHolderName,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Expires: ${paymentMethod.expiryDate}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (paymentMethod.isDefault) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Default",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    // Add new payment method button
                    OutlinedButton(
                        onClick = { /* Navigate to add payment method screen */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD PAYMENT METHOD")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("BACK")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f),
                enabled = selectedPaymentMethod != null
            ) {
                Text("CONTINUE")
            }
        }
    }
}

@Composable
fun OrderSummaryStep(
    cartItems: List<com.example.arfurnitureapp.viewmodel.CartItem>,
    totalPrice: Double,
    checkoutViewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onPlaceOrder: () -> Unit
) {
    val selectedAddress by checkoutViewModel.selectedAddress.collectAsState()
    val selectedPaymentMethod by checkoutViewModel.selectedPaymentMethod.collectAsState()
    val errorMessage by checkoutViewModel.errorMessage.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Order Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error message
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Items summary
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Items (${cartItems.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.product.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$${String.format("%.2f", item.product.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shipping address
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shipping Address",
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextButton(
                        onClick = { checkoutViewModel.previousStep() }
                    ) {
                        Text("Change")
                    }
                }

                if (selectedAddress != null) {
                    val address = selectedAddress!!
                    Text(
                        text = address.fullName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = address.addressLine1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${address.town}, ${address.county} ${address.postcode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = address.country,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment method
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleMedium
                    )

                    TextButton(
                        onClick = { checkoutViewModel.previousStep() }
                    ) {
                        Text("Change")
                    }
                }

                if (selectedPaymentMethod != null) {
                    val payment = selectedPaymentMethod!!
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = payment.cardNumber,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = payment.cardHolderName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Expires: ${payment.expiryDate}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price summary
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subtotal",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tax (8%)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$${String.format("%.2f", totalPrice * 0.08)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Shipping",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (totalPrice > 50) "FREE" else "$9.99",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    val shipping = if (totalPrice > 50) 0.0 else 9.99
                    Text(
                        text = "$${String.format("%.2f", totalPrice + (totalPrice * 0.08) + shipping)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("BACK")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onPlaceOrder,
                modifier = Modifier.weight(1f)
            ) {
                Text("PLACE ORDER")
            }
        }
    }
}

@Composable
fun OrderConfirmationStep(
    orderSummary: OrderSummary?,
    onFinish: () -> Unit
) {
    if (orderSummary == null) {
        // Error state
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFinish
            ) {
                Text("RETURN TO HOME")
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success icon and message
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Order Placed Successfully!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your order has been placed and is being processed",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Order details
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Order #${orderSummary.orderId}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Date: ${orderSummary.orderDate}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Items:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                orderSummary.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.product.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "£${String.format("%.2f", item.product.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "£${String.format("%.2f", orderSummary.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shipping info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Shipping To:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                val address = orderSummary.shippingAddress
                Text(
                    text = address.fullName,
                    style = MaterialTheme.typography.bodyMedium
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
                    text = "${address.town}${if (address.county.isNotBlank()) ", ${address.county}" else ""}",
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Estimated delivery
        Text(
            text = "Estimated Delivery: 3-5 Working Days",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CONTINUE SHOPPING")
        }
    }
}