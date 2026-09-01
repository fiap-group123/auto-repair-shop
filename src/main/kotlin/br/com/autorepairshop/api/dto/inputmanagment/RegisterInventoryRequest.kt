package br.com.autorepairshop.api.dto.inputmanagment

import java.math.BigDecimal

data class RegisterInventoryRequest(
    val name: String,
    val kind: String,
    val unitPrice: BigDecimal,
    val stock: Int,
)
