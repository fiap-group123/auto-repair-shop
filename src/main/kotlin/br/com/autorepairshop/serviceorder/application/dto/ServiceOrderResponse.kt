package br.com.autorepairshop.serviceorder.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ServiceOrderResponse(
    val id: UUID,
    val customerId: UUID,
    val vehicleId: UUID,
    val status: String,
    val openedAt: Instant,
    val items: List<ServiceOrderItemResponse>,
    val total: BigDecimal,
)
