package br.com.autorepairshop.serviceorder.application.dto

import br.com.autorepairshop.catalog.domain.aggregate.Service
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.shared.domain.Money

fun ServiceOrder.toResponse(catalog: List<Service> = emptyList()) = ServiceOrderResponse(
    id = id.value,
    customerId = customerId,
    vehicleId = vehicleId,
    serviceIds = catalog.map { it.id.value },
    status = status.name,
    registeredAt = registeredAt,
    openedAt = openedAt,
    finishedAt = finishedAt,
    total = catalogTotal(catalog = catalog).amount,
)

private fun catalogTotal(catalog: List<Service>): Money = catalog.fold(initial = Money.ZERO) { acc, service ->
    acc.plus(other = service.basePrice)
}
