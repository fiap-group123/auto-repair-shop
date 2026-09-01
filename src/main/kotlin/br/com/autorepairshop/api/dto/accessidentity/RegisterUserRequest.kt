package br.com.autorepairshop.api.dto.authentication

import java.util.UUID

data class RegisterUserRequest(
    val email: String,
    val password: String,
    val role: String,
    val customerId: UUID? = null,
)
