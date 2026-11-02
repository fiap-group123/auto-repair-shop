package br.com.autorepairshop.serviceandexecution.application

import br.com.autorepairshop.serviceandexecution.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderStatus

object ServiceOrderQueue {
    private val hidden = setOf(
        ServiceOrderStatus.FINISHED,
        ServiceOrderStatus.DELIVERED,
    )

    private val priority = listOf(
        ServiceOrderStatus.IN_EXECUTION,
        ServiceOrderStatus.BUDGET_APPROVED,
        ServiceOrderStatus.WAITING_APPROVAL,
        ServiceOrderStatus.IN_DIAGNOSIS,
        ServiceOrderStatus.RECEIVED,
        ServiceOrderStatus.BUDGET_REJECTED,
    )

    fun arrange(orders: List<ServiceOrder>): List<ServiceOrder> = orders
        .filter { order -> order.status !in hidden }
        .sortedBy { order -> order.createdAt }
        .sortedBy { order -> priority.indexOf(element = order.status) }
}
