package br.com.autorepairshop.inputmanagment.application.dto

import java.util.UUID

data class RegisterPartCommand(
    val serviceOrderId: UUID,
    val inventoryId: UUID,
    val quantity: Int,
)
