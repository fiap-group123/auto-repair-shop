package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal
import java.util.UUID

data class OfferedServiceResponse(
    val id: UUID,
    val name: String,
    val price: BigDecimal,
    val active: Boolean,
)
