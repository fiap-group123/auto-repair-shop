package br.com.autorepairshop.api.security

import br.com.autorepairshop.api.dto.authentication.AuthenticatedUser
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.Role
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CurrentUser {
    fun get(): AuthenticatedUser {
        val jwt = SecurityContextHolder.getContext().authentication?.principal as? Jwt
            ?: throw AuthenticationException.Unauthenticated(message = "Authentication required.")
        val subject = jwt.subject
            ?: throw AuthenticationException.Unauthenticated(message = "JWT subject is missing.")
        val roleClaim = jwt.getClaimAsString("role")
            ?: throw AuthenticationException.Unauthenticated(message = "JWT role claim is missing.")
        val role = runCatching { Role.valueOf(value = roleClaim) }.getOrElse(onFailure = {
            throw AuthenticationException.InvalidRole(message = "Unknown role: $roleClaim")
        })
        return AuthenticatedUser(
            userId = UUID.fromString(subject),
            role = role,
            customerId = jwt.getClaimAsString("customerId")?.let(block = UUID::fromString),
        )
    }
}
