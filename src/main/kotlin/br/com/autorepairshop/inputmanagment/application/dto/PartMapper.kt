package br.com.autorepairshop.inputmanagment.application.dto

import br.com.autorepairshop.inputmanagment.domain.aggregate.Part
import kotlin.time.toJavaInstant

fun Part.toResponse() = PartResponse(
    id = id.value,
    serviceOrderId = serviceOrderId,
    inventoryId = inventoryId.value,
    quantity = quantity,
    unitPrice = unitPrice.amount,
    createdAt = createdAt.toJavaInstant(),
)
