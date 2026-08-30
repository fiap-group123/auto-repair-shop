package br.com.autorepairshop.authentication.application.dto

data class CompleteInviteCommand(
    val token: String,
    val email: String,
    val password: String,
)
