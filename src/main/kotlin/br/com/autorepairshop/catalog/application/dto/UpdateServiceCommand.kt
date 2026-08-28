package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal
import java.util.UUID

data class UpdateOfferedServiceCommand(
    val serviceId: UUID,
    val name: String? = null,
    val price: BigDecimal? = null,
)
