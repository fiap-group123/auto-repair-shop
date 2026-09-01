package br.com.autorepairshop.inputmanagment.application.dto

import br.com.autorepairshop.inputmanagment.domain.aggregate.Inventory
import kotlin.time.toJavaInstant

fun Inventory.toResponse() = InventoryResponse(
    id = id.value,
    name = name.value,
    kind = type.name,
    unitPrice = unitPrice.amount,
    stock = stock,
    active = active,
    createdAt = createdAt.toJavaInstant(),
)
