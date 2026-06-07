package com.example.seller.ui.shops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seller.data.local.ShopEntity
import com.example.seller.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShopsViewModel(private val repository: OrderRepository) : ViewModel() {

    private val _state = MutableStateFlow(ShopsState())
    val state = _state.asStateFlow()

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