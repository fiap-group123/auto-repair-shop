package br.com.autorepairshop.authentication.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class AuthenticationException(message: String) : DomainException(message = message) {
    class InvalidCredentials(message: String) : AuthenticationException(message = message)
    class UserAlreadyExists(message: String) : AuthenticationException(message = message)
    class CustomerAlreadyHasUser(message: String) : AuthenticationException(message = message)
    class UserNotFound(message: String) : AuthenticationException(message = message)
    class UserInactive(message: String) : AuthenticationException(message = message)
    class UserAlreadyActive(message: String) : AuthenticationException(message = message)
    class InvalidEmail(message: String) : AuthenticationException(message = message)
    class InvalidPassword(message: String) : AuthenticationException(message = message)
    class InvalidRole(message: String) : AuthenticationException(message = message)
    class Forbidden(message: String) : AuthenticationException(message = message)
    class Unauthenticated(message: String) : AuthenticationException(message = message)
    class InviteNotFound(message: String) : AuthenticationException(message = message)
    class InviteExpired(message: String) : AuthenticationException(message = message)
    class InviteConsumed(message: String) : AuthenticationException(message = message)
    class LinkedCustomerNotFound(message: String) : AuthenticationException(message = message)
    class LinkedCustomerInactive(message: String) : AuthenticationException(message = message)
    class InvalidRefresh(message: String) : AuthenticationException(message = message)
    class RefreshReuse(message: String) : AuthenticationException(message = message)
}
