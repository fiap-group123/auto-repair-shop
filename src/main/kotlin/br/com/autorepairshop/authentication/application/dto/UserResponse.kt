package br.com.autorepairshop.authentication.application.dto

import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val customerId: UUID?,
    val email: String,
    val role: String,
    val active: Boolean,
    val createdAt: Instant,
)
