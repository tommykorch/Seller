package com.example.seller.domain.usecase

import com.example.seller.data.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import com.example.seller.data.local.ShopEntity

class GetShopsUseCase(private val repository: OrderRepository) {
    operator fun invoke(): Flow<List<ShopEntity>> = repository.getShops()
}