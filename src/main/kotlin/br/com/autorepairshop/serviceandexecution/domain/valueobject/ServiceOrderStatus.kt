package br.com.autorepairshop.serviceandexecution.domain.valueobject

enum class ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSIS,
    WAITING_APPROVAL,
    BUDGET_APPROVED,
    IN_EXECUTION,
    FINISHED,
    DELIVERED,
    BUDGET_REJECTED,
}
