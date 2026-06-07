package com.example.seller.ui.shops

import com.example.seller.data.local.ShopEntity

data class ShopsState(
    val shops: List<ShopEntity> = emptyList(),
    val isSaving: Boolean = false
)

sealed class ShopsIntent {
    object LoadShops : ShopsIntent()
    data class AddShop(val name: String, val token: String, val type: String) : ShopsIntent()
    data class DeleteShop(val id: Int) : ShopsIntent()
}