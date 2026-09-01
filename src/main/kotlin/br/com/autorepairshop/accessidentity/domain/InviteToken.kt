package br.com.autorepairshop.accessidentity.domain

object InviteToken {
    fun generate(): String = SecureToken.generate()

    fun hash(raw: String): String = SecureToken.hash(raw = raw)
}
