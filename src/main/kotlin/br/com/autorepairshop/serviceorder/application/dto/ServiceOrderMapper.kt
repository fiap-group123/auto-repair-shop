package br.com.autorepairshop.serviceorder.application.dto

import br.com.autorepairshop.catalog.domain.aggregate.Service
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import kotlin.time.toJavaInstant

fun ServiceOrder.toResponse(catalog: List<Service> = emptyList()) = ServiceOrderResponse(
    id = id.value,
    customerId = customerId,
    vehicleId = vehicleId,
    serviceIds = catalog.map { it.id.value },
    status = status.name,
    createdAt = createdAt.toJavaInstant(),
    startedAt = startedAt?.toJavaInstant(),
    finishedAt = finishedAt?.toJavaInstant(),
    estimatedTime = estimatedTime?.inWholeSeconds,
)
