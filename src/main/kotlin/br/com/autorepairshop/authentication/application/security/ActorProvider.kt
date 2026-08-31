package br.com.autorepairshop.authentication.application.security

interface ActorProvider {
    fun current(): Actor?
}
