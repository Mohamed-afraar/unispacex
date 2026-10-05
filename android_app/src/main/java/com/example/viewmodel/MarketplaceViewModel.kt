package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ServiceRepository
import com.example.model.CampusMeetupLocation
import com.example.model.CampusPaymentMethod
import com.example.model.CartItem
import com.example.model.OrderProcess
import com.example.model.OrderProgressStep
import com.example.model.Product
import com.example.model.Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketplaceUiState(
    val products: List<Product> = emptyList(),
    val services: List<Service> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val showCartSheet: Boolean = false,
    val meetupLocations: List<CampusMeetupLocation> = SampleData.sampleMeetupLocations,
    val selectedMeetupLocation: CampusMeetupLocation = SampleData.sampleMeetupLocations.first(),
    val selectedPaymentMethod: CampusPaymentMethod = CampusPaymentMethod.CASH_ON_HANDOVER,
    val orderMeetupNote: String = "",
    val orders: List<OrderProcess> = emptyList(),
    val selectedProduct: Product? = null,
    val selectedService: Service? = null
) {
    val cartItemCount: Int get() = cartItems.sumOf { it.quantity }
    val cartSubtotal: Int get() = cartItems.sumOf { it.product.price * it.quantity }
}

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val serviceRepository: ServiceRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.observeAll().collect { prods ->
                _uiState.update { it.copy(products = prods) }
            }
        }
        viewModelScope.launch {
            serviceRepository.observeAll().collect { servs ->
                _uiState.update { it.copy(services = servs) }
            }
        }
        viewModelScope.launch {
            orderRepository.cartItemsFlow.collect { items ->
                _uiState.update { it.copy(cartItems = items) }
            }
        }
        viewModelScope.launch {
            orderRepository.ordersFlow.collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        orderRepository.addToCart(product, quantity)
    }

    fun updateCartItemQuantity(productId: String, delta: Int) {
        orderRepository.updateCartItemQuantity(productId, delta)
    }

    fun openCartSheet() {
        _uiState.update { it.copy(showCartSheet = true) }
    }

    fun closeCartSheet() {
        _uiState.update { it.copy(showCartSheet = false) }
    }

    fun selectMeetupLocation(loc: CampusMeetupLocation) {
        _uiState.update { it.copy(selectedMeetupLocation = loc) }
    }

    fun selectPaymentMethod(method: CampusPaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun setMeetupNote(note: String) {
        _uiState.update { it.copy(orderMeetupNote = note) }
    }

    fun placeOrder(clientName: String) {
        val state = _uiState.value
        val primaryItem = state.cartItems.firstOrNull() ?: return
        val newOrder = OrderProcess(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            title = if (state.cartItems.size == 1) primaryItem.product.title else "${primaryItem.product.title} + ${state.cartItems.size - 1} more",
            clientName = clientName.ifBlank { "Campus Student" },
            providerName = primaryItem.product.businessName,
            amount = state.cartSubtotal,
            currentStep = OrderProgressStep.REQUEST,
            date = "Today"
        )
        orderRepository.addOrder(newOrder)
        orderRepository.clearCart()
        _uiState.update { it.copy(showCartSheet = false) }
        viewModelScope.launch {
            orderRepository.syncOrderToFirestore(newOrder)
        }
    }

    fun advanceOrderStep(orderId: String) {
        val currentOrder = _uiState.value.orders.firstOrNull { it.orderId == orderId } ?: return
        val nextStep = when (currentOrder.currentStep) {
            OrderProgressStep.REQUEST -> OrderProgressStep.QUOTE
            OrderProgressStep.QUOTE -> OrderProgressStep.ACCEPTED
            OrderProgressStep.ACCEPTED -> OrderProgressStep.IN_PROGRESS
            OrderProgressStep.IN_PROGRESS -> OrderProgressStep.COMPLETED
            OrderProgressStep.COMPLETED -> OrderProgressStep.REVIEW
            OrderProgressStep.REVIEW -> OrderProgressStep.REVIEW
        }
        orderRepository.updateOrderStatus(orderId, nextStep)
    }
}
