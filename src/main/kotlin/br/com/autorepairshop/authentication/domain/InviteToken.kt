package br.com.autorepairshop.authentication.domain

object InviteToken {
    fun generate(): String = SecureToken.generate()

    fun hash(raw: String): String = SecureToken.hash(raw = raw)
}
