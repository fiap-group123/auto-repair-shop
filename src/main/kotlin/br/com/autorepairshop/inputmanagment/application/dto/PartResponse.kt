package br.com.autorepairshop.inputmanagment.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PartResponse(
    val id: UUID,
    val serviceOrderId: UUID,
    val inventoryId: UUID,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val createdAt: Instant,
)
