package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ExtraServiceResponse(
    val id: UUID,
    val serviceOrderId: UUID,
    val name: String,
    val basePrice: BigDecimal,
    val status: String,
    val createdAt: Instant,
)
