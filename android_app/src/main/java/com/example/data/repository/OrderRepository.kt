package com.example.data.repository

import com.example.data.SampleData
import com.example.model.CampusMeetupLocation
import com.example.model.CampusPaymentMethod
import com.example.model.CartItem
import com.example.model.OrderProcess
import com.example.model.OrderProgressStep
import com.example.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItemsFlow: Flow<List<CartItem>> = _cartItems.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderProcess>>(emptyList())
    val ordersFlow: Flow<List<OrderProcess>> = _orders.asStateFlow()

    val meetupLocations: List<CampusMeetupLocation> = SampleData.sampleMeetupLocations

    fun addToCart(product: Product, quantity: Int = 1, note: String = "") {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity, note = note))
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(productId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun addOrder(order: OrderProcess) {
        _orders.value = listOf(order) + _orders.value
    }

    fun updateOrderStatus(orderId: String, nextStep: OrderProgressStep) {
        _orders.value = _orders.value.map { order ->
            if (order.orderId == orderId) order.copy(currentStep = nextStep) else order
        }
    }

    suspend fun syncOrderToFirestore(order: OrderProcess) {
        try {
            val orderData = hashMapOf(
                "orderId" to order.orderId,
                "title" to order.title,
                "clientName" to order.clientName,
                "providerName" to order.providerName,
                "amount" to order.amount,
                "currentStep" to order.currentStep.name,
                "date" to order.date,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("orders").document(order.orderId).set(orderData).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync order to Firestore")
        }
    }
}
