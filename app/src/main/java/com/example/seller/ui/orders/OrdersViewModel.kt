package com.example.seller.ui.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seller.domain.model.OrderStatus
import com.example.seller.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val fetchAllUseCase: FetchAllOrdersUseCase,
    private val getOrdersUseCase: GetOrdersByStatusUseCase,
    private val moveOrderUseCase: MoveOrderUseCase,
    private val syncArchiveUseCase: SyncArchiveUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state.asStateFlow()
    private var observationJob: kotlinx.coroutines.Job? = null
    fun handleIntent(intent: OrdersIntent) {
        when (intent) {
            is OrdersIntent.LoadNew -> {
                observeOrders(OrderStatus.NEW.value)
                refreshNewOrdersFromApi()
            }
            is OrdersIntent.LoadArchive -> {
                observeOrders("ARCHIVE")
                refreshArchiveFromApi()
            }
            is OrdersIntent.SendToAssembly -> {
                viewModelScope.launch {
                    moveOrderUseCase(intent.id, OrderStatus.ASSEMBLY)
                }
            }
        }
    }

    private fun observeOrders(status: String) {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            getOrdersUseCase(status).collect { list ->
                _state.update { it.copy(orders = list) }
            }
        }
    }

    private fun refreshNewOrdersFromApi() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                fetchAllUseCase()

                kotlinx.coroutines.delay(500)
                val newOrdersCount = getOrdersUseCase(OrderStatus.NEW.value).first().size

                if (newOrdersCount == 0) {
                    Log.d("VM_LOG", "Новых заказов нет, загружаю архив...")
                    syncArchiveUseCase() // Автоматически вызываем архив
                }

            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun refreshArchiveFromApi() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                syncArchiveUseCase()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}