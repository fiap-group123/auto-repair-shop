package br.com.autorepairshop.serviceandexecution.infrastructure.persistence

enum class ServiceOrderStatusColumn {
    RECEIVED,
    IN_DIAGNOSIS,
    WAITING_APPROVAL,
    BUDGET_APPROVED,
    BUDGET_REJECTED,
    IN_EXECUTION,
    FINISHED,
    DELIVERED,
}
