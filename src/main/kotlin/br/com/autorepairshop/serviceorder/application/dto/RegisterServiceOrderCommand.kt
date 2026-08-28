package br.com.autorepairshop.serviceorder.application.dto

import java.util.UUID

data class RegisterServiceOrderCommand(
    val customerId: UUID,
    val vehicleId: UUID,
)
