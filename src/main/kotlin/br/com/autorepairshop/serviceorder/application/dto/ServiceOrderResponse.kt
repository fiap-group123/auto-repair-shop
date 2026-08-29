package br.com.autorepairshop.serviceorder.application.dto

import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Instant

data class ServiceOrderResponse(
    val id: UUID,
    val customerId: UUID,
    val vehicleId: UUID,
    val serviceIds: List<UUID>,
    val status: String,
    val registeredAt: Instant,
    val openedAt: Instant?,
    val finishedAt: Instant?,
    val estimateTime: Duration?,
    val total: BigDecimal,
)
