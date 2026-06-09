package com.example.seller.ui.orders

import com.example.seller.data.local.OrderEntity

// Состояние экрана
data class OrdersState(
    val orders: List<OrderEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Команды пользователя (Интенты)
sealed class OrdersIntent {
    object LoadNew : OrdersIntent()           // Загрузить новые заказы
    object LoadArchive : OrdersIntent()       // Загрузить архив
    data class SendToAssembly(val id: Long) : OrdersIntent() // Кнопка "В сборку"

}