package br.com.autorepairshop.serviceorder.infrastructure.persistence

enum class ServiceOrderStatusColumn {
    RECEIVED,
    IN_DIAGNOSIS,
    WAITING_APPROVAL,
    IN_EXECUTION,
    FINISHED,
    DELIVERED,
}
