package br.com.autorepairshop.accessidentity.application.dto

import java.time.Instant

data class CustomerInviteResponse(
    val customerName: String,
    val expiresAt: Instant,
)
