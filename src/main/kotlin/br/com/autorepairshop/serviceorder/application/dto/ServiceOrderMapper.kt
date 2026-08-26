package br.com.autorepairshop.serviceorder.application.dto

import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderItem
import kotlin.time.toJavaInstant

fun ServiceOrder.toResponse() = ServiceOrderResponse(
    id = id.value,
    customerId = customerId,
    vehicleId = vehicleId,
    status = status.name,
    openedAt = openedAt.toJavaInstant(),
    items = items.map { it.toResponse() },
    total = total().amount,
)

fun ServiceOrderItem.toResponse() = ServiceOrderItemResponse(
    offeredServiceId = offeredServiceId,
    description = description,
    unitPrice = unitPrice.amount,
    quantity = quantity,
    subtotal = subtotal().amount,
)
