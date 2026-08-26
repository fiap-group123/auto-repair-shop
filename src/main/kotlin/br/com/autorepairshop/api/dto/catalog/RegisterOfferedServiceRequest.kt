package br.com.autorepairshop.api.dto.catalog

import java.math.BigDecimal

data class RegisterOfferedServiceRequest(
    val name: String,
    val price: BigDecimal,
)
