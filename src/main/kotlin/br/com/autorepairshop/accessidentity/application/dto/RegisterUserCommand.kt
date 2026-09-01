package br.com.autorepairshop.accessidentity.application.dto

import java.util.UUID

data class RegisterUserCommand(
    val email: String,
    val password: String,
    val role: String,
    val customerId: UUID? = null,
)
