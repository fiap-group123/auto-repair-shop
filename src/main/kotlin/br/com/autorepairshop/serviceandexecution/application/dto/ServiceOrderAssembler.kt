package br.com.autorepairshop.serviceandexecution.application.dto

import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.serviceandexecution.domain.aggregate.ServiceOrder
import org.springframework.stereotype.Component

@Component
class ServiceOrderAssembler(
    private val services: ServiceRepository,
    private val parts: PartRepository,
) {

    fun toResponse(order: ServiceOrder): ServiceOrderResponse = order.toResponse(
        catalog = services.findByServiceOrderId(serviceOrderId = order.id.value),
        parts = parts.findByServiceOrderId(serviceOrderId = order.id.value),
    )

    fun toResponses(orders: List<ServiceOrder>): List<ServiceOrderResponse> {
        val orderIds = orders.map { it.id.value }
        val catalog = services.findByServiceOrderIds(serviceOrderIds = orderIds)
        val partLines = parts.findByServiceOrderIds(serviceOrderIds = orderIds)
        val catalogByOrder = catalog.groupBy { it.serviceOrderId }
        val partsByOrder = partLines.groupBy { it.serviceOrderId }
        return orders.map { order ->
            order.toResponse(
                catalog = catalogByOrder[order.id.value].orEmpty(),
                parts = partsByOrder[order.id.value].orEmpty(),
            )
        }
    }
}
