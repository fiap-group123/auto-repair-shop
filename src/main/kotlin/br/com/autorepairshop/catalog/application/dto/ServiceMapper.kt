package br.com.autorepairshop.catalog.application.dto

import br.com.autorepairshop.catalog.domain.aggregate.Service

fun Service.toResponse() = ServiceResponse(
    id = id.value,
    serviceOrderId = serviceOrderId,
    name = name.value,
    price = basePrice.amount,
    active = active,
)
