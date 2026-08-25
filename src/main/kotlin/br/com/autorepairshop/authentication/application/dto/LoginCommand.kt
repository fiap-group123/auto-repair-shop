package br.com.autorepairshop.authentication.application.dto

data class LoginCommand(
    val email: String,
    val password: String,
)
