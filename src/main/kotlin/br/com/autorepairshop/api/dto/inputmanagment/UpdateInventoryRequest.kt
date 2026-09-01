package br.com.autorepairshop.api.dto.inputmanagment

import java.math.BigDecimal

data class UpdateInventoryRequest(
    val name: String?,
    val unitPrice: BigDecimal?,
    val kind: String?,
)
