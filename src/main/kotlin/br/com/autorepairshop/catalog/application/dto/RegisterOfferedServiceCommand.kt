package br.com.autorepairshop.catalog.application.dto

import java.math.BigDecimal

data class RegisterOfferedServiceCommand(
    val name: String,
    val price: BigDecimal,
)
