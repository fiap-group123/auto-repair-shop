package br.com.autorepairshop.serviceorder.domain.valueobject

enum class ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSIS,
    WAITING_APPROVAL,
    IN_EXECUTION,
    COMPLETED,
    DELIVERED,
}
