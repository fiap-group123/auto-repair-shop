package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ServiceResponse(
    val id: UUID,
    val serviceOrderId: UUID,
    val name: String,
    val basePrice: BigDecimal,
    val status: String,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val estimatedTime: Long?,
)
