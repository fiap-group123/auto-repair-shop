package br.com.autorepairshop.inputmanagment.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class InventoryResponse(
    val id: UUID,
    val name: String,
    val kind: String,
    val unitPrice: BigDecimal,
    val stock: Int,
    val active: Boolean,
    val createdAt: Instant,
)
