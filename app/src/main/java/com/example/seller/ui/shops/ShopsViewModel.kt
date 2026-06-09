package com.example.seller.ui.shops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seller.data.local.ShopEntity
import com.example.seller.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.seller.domain.usecase.DeleteShopUseCase

class ShopsViewModel(private val repository: OrderRepository,private val deleteShopUseCase: DeleteShopUseCase) : ViewModel() {

    private val _state = MutableStateFlow(ShopsState())
    val state = _state.asStateFlow()
    fun deleteShop(shop: ShopEntity) {
        viewModelScope.launch {
            try {
                deleteShopUseCase(shop.id, shop.name)
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }
    fun handleIntent(intent: ShopsIntent) {
        when (intent) {
            is ShopsIntent.LoadShops -> {
                viewModelScope.launch {
                    repository.getShops().collect { list ->
                        _state.update { it.copy(shops = list) }
                    }
                }
            }
            is ShopsIntent.AddShop -> {
                viewModelScope.launch {
                    _state.update { it.copy(isSaving = true) }
                    repository.saveShop(ShopEntity(name = intent.name, token = intent.token, type = intent.type))
                    _state.update { it.copy(isSaving = false) }
                }
            }
            is ShopsIntent.DeleteShop -> {
                viewModelScope.launch {
                    repository.deleteShop(intent.id)
                }
            }
        }
    }
}