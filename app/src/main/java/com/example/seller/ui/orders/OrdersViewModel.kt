package com.example.seller.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seller.domain.model.OrderStatus
import com.example.seller.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val fetchAllUseCase: FetchAllOrdersUseCase,
    private val getOrdersUseCase: GetOrdersByStatusUseCase,
    private val moveOrderUseCase: MoveOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state.asStateFlow()

    fun handleIntent(intent: OrdersIntent) {
        when (intent) {
            is OrdersIntent.LoadNew -> {
                observeOrders(OrderStatus.NEW.value)
                refreshFromApi()
            }
            is OrdersIntent.LoadArchive -> {
                observeOrders(OrderStatus.ASSEMBLY.value)
            }
            is OrdersIntent.SendToAssembly -> {
                viewModelScope.launch {
                    moveOrderUseCase(intent.id, OrderStatus.ASSEMBLY)
                }
            }
        }
    }

    private fun observeOrders(status: String) {
        viewModelScope.launch {
            getOrdersUseCase(status).collect { list ->
                _state.update { it.copy(orders = list) }
            }
        }
    }

    private fun refreshFromApi() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                fetchAllUseCase()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}