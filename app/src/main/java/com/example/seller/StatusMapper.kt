package com.example.seller

object StatusMapper {
    private val supplierStatusMap = mapOf(
        "new" to "Новое сборочное задание",
        "confirm" to "На сборке",
        "deliver" to "В доставке",
        "receive" to "Получено покупателем",
        "reject" to "Отказ покупателя при получении",
        "cancel" to "Отменено продавцом",
        "cancel_missed_call" to "Отмена заказа по причине недозвона"
    )

    private val wbStatusMap = mapOf(
        "waiting" to "В работе",
        "sorted" to "Отсортировано",
        "sold" to "Получено покупателем",
        "canceled" to "Отменено",
        "canceled_by_client" to "Покупатель отменил заказ при получении",
        "declined_by_client" to "Покупатель отменил заказ в первый час",
        "defect" to "Отменено по причине брака",
        "ready_for_pickup" to "Прибыло на ПВЗ",
        "canceled_by_missed_call" to "Отмена по причине недозвона"
    )

    fun mapSupplier(status: String?): String = supplierStatusMap[status] ?: status ?: "Неизвестно"
    fun mapWb(status: String?): String = wbStatusMap[status] ?: status ?: "Неизвестно"
}