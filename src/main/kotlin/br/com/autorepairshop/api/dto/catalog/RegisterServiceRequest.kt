package br.com.autorepairshop.api.dto.catalog

import java.math.BigDecimal
import java.util.UUID

data class RegisterOfferedServiceRequest(
    val serviceOrderId: UUID,
    val name: String,
    val price: BigDecimal,
)
