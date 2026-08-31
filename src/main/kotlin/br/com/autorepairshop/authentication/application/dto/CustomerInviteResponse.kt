package br.com.autorepairshop.authentication.application.dto

import java.time.Instant

data class CustomerInviteResponse(
    val customerName: String,
    val expiresAt: Instant,
)
