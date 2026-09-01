package br.com.autorepairshop.api.dto.catalog

import java.math.BigDecimal
import java.util.UUID

data class RegisterExtraServiceRequest(
    val serviceOrderId: UUID,
    val name: String,
    val basePrice: BigDecimal,
)
