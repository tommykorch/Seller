package com.example.seller.domain.usecase

import com.example.seller.data.repository.OrderRepository
import kotlinx.coroutines.flow.first

class FetchAllOrdersUseCase(
    private val repository: OrderRepository,
    private val getShopsUseCase: GetShopsUseCase
) {
    suspend operator fun invoke() {
        // Берем актуальный список магазинов из БД и для каждого обновляем заказы
        val shops = getShopsUseCase().first()
        shops.forEach { shop ->
            repository.refreshOrdersForShop(shop)
        }
    }
}