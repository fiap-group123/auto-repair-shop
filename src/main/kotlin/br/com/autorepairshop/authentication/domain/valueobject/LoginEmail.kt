package br.com.autorepairshop.authentication.domain.valueobject

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class LoginEmail private constructor(val value: String) : ValueObject {
    companion object {
        fun of(raw: String): LoginEmail {
            val address = raw.trim().lowercase()
            if (address.length !in 5..60) {
                throw AuthenticationException.InvalidEmail(
                    message = "Email address must be between 5 and 60 characters.",
                )
            }
            if (!Regex(pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                    .matches(input = address)
            ) {
                throw AuthenticationException.InvalidEmail(message = "Invalid email address format.")
            }
            return LoginEmail(value = address)
        }
    }
}
