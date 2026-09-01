package br.com.autorepairshop.catalog.application.dto

import br.com.autorepairshop.catalog.domain.aggregate.ExtraService
import kotlin.time.toJavaInstant

fun ExtraService.toResponse() = ExtraServiceResponse(
    id = id.value,
    serviceOrderId = serviceOrderId,
    name = name.value,
    basePrice = basePrice.amount,
    status = status.name,
    createdAt = createdAt.toJavaInstant(),
)
