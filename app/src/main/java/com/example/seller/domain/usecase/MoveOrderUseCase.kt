package com.example.seller.domain.usecase

import com.example.seller.data.repository.OrderRepository
import com.example.seller.domain.model.OrderStatus

class MoveOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: Long, newStatus: OrderStatus) {
        repository.getOrders(newStatus.value)
    }
}