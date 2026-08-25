package br.com.autorepairshop.authentication.application.dto

import java.util.UUID

data class RegisterUserCommand(
    val email: String,
    val password: String,
    val role: String,
    val customerId: UUID? = null,
)
