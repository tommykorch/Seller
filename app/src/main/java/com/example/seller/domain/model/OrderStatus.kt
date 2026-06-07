package com.example.seller.domain.model

enum class OrderStatus(val value: String) {
    NEW("NEW"),
    ASSEMBLY("ASSEMBLY"),
    ARCHIVE("ARCHIVE")
}