package br.com.autorepairshop.accessidentity.application.security

interface ActorProvider {
    fun current(): Actor?
}
