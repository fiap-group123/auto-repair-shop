package br.com.autorepairshop.serviceorder.application.dto

import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import org.springframework.stereotype.Component

@Component
class ServiceOrderAssembler(private val services: ServiceRepository) {

    fun toResponse(order: ServiceOrder): ServiceOrderResponse =
        order.toResponse(catalog = services.findByServiceOrderId(serviceOrderId = order.id.value))

    fun toResponses(orders: List<ServiceOrder>): List<ServiceOrderResponse> {
        val catalog = services.findByServiceOrderIds(serviceOrderIds = orders.map { it.id.value })
        val byOrder = catalog.groupBy { it.serviceOrderId }
        return orders.map { it.toResponse(catalog = byOrder[it.id.value].orEmpty()) }
    }
}
