package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal
import java.util.UUID

data class UpdateServiceCommand(
    val serviceId: UUID,
    val name: String? = null,
    val basePrice: BigDecimal? = null,
)
