package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal
import java.util.UUID

data class RegisterServiceCommand(
    val serviceOrderId: UUID,
    val name: String,
    val basePrice: BigDecimal,
)
