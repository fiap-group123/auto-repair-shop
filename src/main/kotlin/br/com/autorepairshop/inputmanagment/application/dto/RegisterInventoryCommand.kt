package br.com.autorepairshop.inputmanagment.application.dto

import java.math.BigDecimal

data class RegisterInventoryCommand(
    val name: String,
    val kind: String,
    val unitPrice: BigDecimal,
    val stock: Int,
)
