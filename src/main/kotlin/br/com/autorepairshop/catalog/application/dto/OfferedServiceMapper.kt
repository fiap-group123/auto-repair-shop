package br.com.autorepairshop.catalog.application.dto

import br.com.autorepairshop.catalog.domain.aggregate.OfferedService

fun OfferedService.toResponse() = OfferedServiceResponse(
    id = id.value,
    name = name.value,
    price = price.amount,
    active = active,
)
