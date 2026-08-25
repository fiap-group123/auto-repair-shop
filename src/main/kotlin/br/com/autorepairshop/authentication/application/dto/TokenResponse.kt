package br.com.autorepairshop.authentication.application.dto

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
)
