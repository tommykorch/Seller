package com.example.seller.domain.usecase
import com.example.seller.data.local.OrderEntity
import com.example.seller.data.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetOrdersByStatusUseCase(private val repository: OrderRepository) {
    operator fun invoke(status: String): Flow<List<OrderEntity>> {
        return repository.getOrders(status)
    }
}