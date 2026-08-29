package br.com.autorepairshop.catalog.application.dto

import br.com.autorepairshop.catalog.domain.aggregate.Service
import kotlin.time.toJavaInstant

fun Service.toResponse() = ServiceResponse(
    id = id.value,
    serviceOrderId = serviceOrderId,
    name = name.value,
    basePrice = basePrice.amount,
    status = status.name,
    createdAt = createdAt.toJavaInstant(),
    startedAt = startedAt?.toJavaInstant(),
    finishedAt = finishedAt?.toJavaInstant(),
    estimatedTime = estimatedTime?.inWholeSeconds,
)
