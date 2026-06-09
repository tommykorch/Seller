package com.example.seller.domain.usecase

import com.example.seller.data.repository.OrderRepository
import java.util.Calendar

class SyncArchiveUseCase(
    private val repository: OrderRepository
) {
    suspend operator fun invoke() {
        val calendar = Calendar.getInstance()
        val dateTo = calendar.timeInMillis / 1000
        calendar.add(Calendar.DAY_OF_YEAR, -30) // период 30 дней
        val dateFrom = calendar.timeInMillis / 1000

        repository.syncAllShopsArchive(dateFrom, dateTo)
    }
}