package com.example.arfurnitureapp.model

import com.example.arfurnitureapp.viewmodel.CartItem
import java.util.Date

enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String = "",
    val totalPrice: Double = price * quantity
)

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val shipping: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val shippingAddress: Address = Address(),
    val billingAddress: Address = Address(),
    val paymentMethodId: String = "",
    val paymentMethodLast4: String = "",
    val orderDate: Date = Date(),
    val status: OrderStatus = OrderStatus.PENDING,
    val estimatedDeliveryDate: Date? = null,
    val trackingNumber: String = "",
    val notes: String = ""
) {
    // Calculate the order total
    fun calculateTotal(): Double {
        return subtotal + tax + shipping - discount
    }
}

data class OrderSummary(
    val orderId: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val shipping: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val shippingAddress: Address = Address(),
    val paymentMethod: PaymentMethod = PaymentMethod(),
    val orderDate: String = "",
    val orderStatus: String = "Processing"
)