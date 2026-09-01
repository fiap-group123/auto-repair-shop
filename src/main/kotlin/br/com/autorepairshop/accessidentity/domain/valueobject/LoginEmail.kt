package br.com.autorepairshop.accessidentity.domain.valueobject

import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.shared.domain.Email
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class LoginEmail private constructor(val value: String) : ValueObject {
    companion object {
        fun of(raw: String): LoginEmail = LoginEmail(
            value = Email.of(
                raw = raw,
                invalid = { AuthenticationException.InvalidEmail(message = it) },
            ).value,
        )
    }
}
