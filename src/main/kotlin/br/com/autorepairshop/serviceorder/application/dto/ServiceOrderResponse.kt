package br.com.autorepairshop.serviceorder.application.dto

import java.time.Instant
import java.util.UUID

data class ServiceOrderResponse(
    val id: UUID,
    val customerId: UUID,
    val vehicleId: UUID,
    val serviceIds: List<UUID>,
    val status: String,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val estimatedTime: Long?,
)
