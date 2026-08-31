package br.com.autorepairshop.serviceorder.domain.valueobject

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
