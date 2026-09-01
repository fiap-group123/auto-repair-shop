package br.com.autorepairshop.inputmanagment.application.dto

import java.util.UUID

data class UpdatePartCommand(
    val partId: UUID,
    val quantity: Int,
)
