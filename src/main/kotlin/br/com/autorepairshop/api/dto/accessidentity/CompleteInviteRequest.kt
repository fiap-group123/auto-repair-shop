package br.com.autorepairshop.api.dto.authentication

data class CompleteInviteRequest(
    val email: String,
    val password: String,
)
