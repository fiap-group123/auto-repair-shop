package br.com.autorepairshop.api.dto.catalog

import java.math.BigDecimal

data class UpdateOfferedServiceRequest(
    val name: String? = null,
    val price: BigDecimal? = null,
)
