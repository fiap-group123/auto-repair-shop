package br.com.autorepairshop.authentication.application.dto

import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val role: String,
    val active: Boolean,
    val customerId: UUID?,
)
