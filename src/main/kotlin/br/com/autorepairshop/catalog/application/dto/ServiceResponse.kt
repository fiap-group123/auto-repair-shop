package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Instant

data class ServiceResponse(
    val id: UUID,
    val serviceOrderId: UUID,
    val name: String,
    val basePrice: BigDecimal,
    val status: String,
    val registeredAt: Instant,
    val openedAt: Instant?,
    val finishedAt: Instant?,
    val estimatedTime: Duration?,
)
