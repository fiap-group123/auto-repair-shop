package br.com.autorepairshop.api.dto.inputmanagment

import java.util.UUID

data class RegisterPartRequest(
    val serviceOrderId: UUID,
    val inventoryId: UUID,
    val quantity: Int,
)
