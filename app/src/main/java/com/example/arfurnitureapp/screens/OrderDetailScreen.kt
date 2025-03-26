package com.example.arfurnitureapp.screens


import com.example.arfurnitureapp.utils.OrderStatusBadge
import com.example.arfurnitureapp.utils.capitalize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arfurnitureapp.model.Order
import com.example.arfurnitureapp.model.OrderStatus
import com.example.arfurnitureapp.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    navController: NavController,
    orderViewModel: OrderViewModel = viewModel()
) {
    val selectedOrder by orderViewModel.selectedOrder.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val error by orderViewModel.error.collectAsState()
    val successMessage by orderViewModel.successMessage.collectAsState()

    // Show error or success message with Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error, successMessage) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            orderViewModel.clearError()
        }

        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            orderViewModel.clearSuccessMessage()
        }
    }

    // Load order details when the screen is shown
    LaunchedEffect(orderId) {
        orderViewModel.getOrderDetails(orderId)
    }

    // Cancel order confirmation dialog
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Order Details")
                        if (selectedOrder != null) {
                            Text(
                                text = "#${selectedOrder!!.orderId.takeLast(8).uppercase()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && selectedOrder == null) {
                // Show loading spinner
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (selectedOrder == null) {
                // Show error state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Order not found",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("GO BACK")
                    }
                }
            } else {
                // Show order details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Order Status and Tracking
                    OrderStatusCard(
                        order = selectedOrder!!,
                        onTrackOrder = {
                            // In a real app, this would navigate to a tracking screen
                            // or open a web browser with the tracking URL
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Order Items
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Order Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            selectedOrder!!.items.forEachIndexed { index, item ->
                                OrderItemRow(item = item)
                                if (index < selectedOrder!!.items.size - 1) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }

                            // Order Summary
                            OrderSummary(order = selectedOrder!!)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shipping Address
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Shipping Address",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val address = selectedOrder!!.shippingAddress
                            Text(
                                text = address.fullName,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            // Display UK address format
                            Text(
                                text = address.phoneNumber,
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Method
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Payment Method",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "•••• •••• •••• ${selectedOrder!!.paymentMethodLast4}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    if (selectedOrder!!.status == OrderStatus.PENDING ||
                        selectedOrder!!.status == OrderStatus.PROCESSING) {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CANCEL ORDER")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BACK TO ORDERS")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Cancel order confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order") },
            text = { Text("Are you sure you want to cancel this order? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        orderViewModel.cancelOrder(selectedOrder!!.orderId)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("CANCEL ORDER")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("KEEP ORDER")
                }
            }
        )
    }
}

@Composable
fun OrderItemRow(item: com.example.arfurnitureapp.model.OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image (if available) or placeholder
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val imageResId = item.imageUrl.toIntOrNull()
            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = item.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PlaceholderForFailedImage(productName = item.productName)
            }
        }

        // Product details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Qty: ${item.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Price
        Text(
            text = "£${String.format("%.2f", item.totalPrice)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PlaceholderForFailedImage(productName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = productName.firstOrNull()?.uppercase() ?: "P",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun OrderStatusCard(
    order: Order,
    onTrackOrder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Order date and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order Placed",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = formatDate(order.orderDate),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery date if shipped or delivered
            if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.DELIVERED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (order.status == OrderStatus.DELIVERED) "Delivered on" else "Estimated Delivery",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = formatDate(order.estimatedDeliveryDate ?: Date()),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Track order button if shipped
                    if (order.status == OrderStatus.SHIPPED && order.trackingNumber.isNotBlank()) {
                        OutlinedButton(onClick = onTrackOrder) {
                            Text("TRACK")
                        }
                    }
                }

                // Tracking number if shipped
                if (order.status == OrderStatus.SHIPPED && order.trackingNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tracking #:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = order.trackingNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Order progress
            OrderProgressIndicator(status = order.status)
        }
    }
}

@Composable
fun OrderProgressIndicator(status: OrderStatus) {
    val steps = listOf(
        OrderStatus.PENDING,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED
    )

    // Don't show progress for cancelled orders
    if (status == OrderStatus.CANCELLED || status == OrderStatus.RETURNED) {
        return
    }

    val currentStepIndex = steps.indexOf(status)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Status circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= currentStepIndex)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index <= currentStepIndex) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Status label
                Text(
                    text = step.name.capitalize(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = if (index <= currentStepIndex)
                        MaterialTheme.colorScheme.onBackground
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Connecting line between circles (except for the last one)
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .background(
                            if (index < currentStepIndex)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@Composable
fun OrderSummary(order: Order) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Subtotal
        OrderSummaryRow(
            label = "Subtotal",
            value = "£${String.format("%.2f", order.subtotal)}"
        )

        // Shipping
        OrderSummaryRow(
            label = "Shipping",
            value = if (order.shipping == 0.0) "FREE" else "£${String.format("%.2f", order.shipping)}"
        )

        // Tax
        OrderSummaryRow(
            label = "Tax",
            value = "£${String.format("%.2f", order.tax)}"
        )

        // Discount (if any)
        if (order.discount > 0) {
            OrderSummaryRow(
                label = "Discount",
                value = "-£${String.format("%.2f", order.discount)}",
                valueColor = MaterialTheme.colorScheme.error
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Total
        OrderSummaryRow(
            label = "Total",
            value = "£${String.format("%.2f", order.total)}",
            isTotal = true
        )
    }
}

@Composable
fun OrderSummaryRow(
    label: String,
    value: String,
    isTotal: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (valueColor != Color.Unspecified) valueColor else Color.Unspecified
        )
    }
}

@Composable


// Helper function for date formatting
fun formatDate(date: Date?): String {
    if (date == null) return "N/A"
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.UK)
    return formatter.format(date)
}

